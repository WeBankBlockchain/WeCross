package com.webank.wecross.routine.htlc;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.exception.WeCrossException.ErrorCode;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.routine.RoutineDefault;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.BlockManager;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.TransactionException;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import java.math.BigInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssetHTLC implements HTLC {
    private Logger logger = LoggerFactory.getLogger(AssetHTLC.class);

    @Override
    public void lockSelf(HTLCResource htlcResource, String hash, Callback callback) {
        sendTransaction(
                htlcResource,
                RoutineDefault.LOCK_METHOD,
                new String[] {hash},
                (exception, result) -> {
                    if (exception != null) {
                        callback.onReturn(exception, null);
                    } else if (!RoutineDefault.SUCCESS_FLAG.equals(result)) {
                        callback.onReturn(
                                new WeCrossException(
                                        ErrorCode.HTLC_ERROR,
                                        "LOCK_SELF_ERROR",
                                        HTLCErrorCode.LOCK_ERROR,
                                        result),
                                null);
                    } else {
                        callback.onReturn(null, result);
                    }
                });
    }

    @Override
    public void lockCounterparty(HTLCResource htlcResource, String hash, Callback callback) {
        transferWithValidation(
                htlcResource, RoutineDefault.LOCK_METHOD, new String[] {hash}, callback);
    }

    @Override
    public void unlockCounterparty(
            HTLCResource htlcResource, String hash, String secret, Callback callback) {
        transferWithValidation(
                htlcResource, RoutineDefault.UNLOCK_METHOD, new String[] {hash, secret}, callback);
    }

    // lock or unlock with validation
    private void transferWithValidation(
            HTLCResource htlcResource, String method, String[] args, Callback callback) {
        TransactionRequest request = new TransactionRequest(method, args);
        htlcResource.asyncSendTransaction(
                request,
                htlcResource.getAccount1(),
                (transactionException, transactionResponse) -> {
                    if (transactionException != null && !transactionException.isSuccess()) {
                        callback.onReturn(
                                new WeCrossException(
                                        ErrorCode.HTLC_ERROR,
                                        method.toUpperCase() + "_COUNTERPARTY_ERROR",
                                        HTLCErrorCode.TRANSACTION_ERROR,
                                        transactionException.getMessage()),
                                null);
                        return;
                    }

                    if (transactionResponse.getErrorCode() != 0) {
                        callback.onReturn(
                                new WeCrossException(
                                        ErrorCode.HTLC_ERROR,
                                        method.toUpperCase() + "_COUNTERPARTY_ERROR",
                                        HTLCErrorCode.TRANSACTION_ERROR,
                                        transactionResponse.getErrorMessage()),
                                null);
                        return;
                    }

                    String res = null;
                    String[] result = transactionResponse.getResult();
                    if (result != null && result.length != 0) {
                        res = result[0].trim();
                    }

                    if (!RoutineDefault.SUCCESS_FLAG.equals(res)) {
                        int errorCode =
                                RoutineDefault.LOCK_METHOD.equals(method)
                                        ? HTLCErrorCode.LOCK_ERROR
                                        : HTLCErrorCode.UNLOCK_ERROR;
                        callback.onReturn(
                                new WeCrossException(
                                        ErrorCode.HTLC_ERROR,
                                        method.toUpperCase() + "_COUNTERPARTY_ERROR",
                                        errorCode,
                                        res),
                                null);
                        return;
                    }

                    VerifyData verifyData =
                            new VerifyData(
                                    transactionResponse.getBlockNumber(),
                                    transactionResponse.getHash(),
                                    method,
                                    args,
                                    new String[] {RoutineDefault.SUCCESS_FLAG});
                    verifyData.setPath(htlcResource.getPath());
                    String finalRes = res;
                    verifyHtlcTransaction(
                            htlcResource,
                            verifyData,
                            (exception, result1) -> {
                                if (exception != null || !result1) {
                                    callback.onReturn(
                                            new WeCrossException(
                                                    ErrorCode.HTLC_ERROR,
                                                    method.toUpperCase() + "_COUNTERPARTY_ERROR",
                                                    HTLCErrorCode.VERIFY_TRANSACTION_ERROR,
                                                    "failed to verify transaction"),
                                            null);
                                } else {
                                    callback.onReturn(null, finalRes);
                                }
                            });
                });
    }

    @Override
    public void rollback(HTLCResource htlcResource, String hash, Callback callback) {
        sendTransaction(
                htlcResource, RoutineDefault.ROLLBACK_METHOD, new String[] {hash}, callback);
    }

    @Override
    public void verifyHtlcTransaction(
            Resource resource, VerifyData verifyData, VerifyCallback callback) {
        String txHash = verifyData.getTransactionHash();
        long blockNumber = verifyData.getBlockNumber();
        BlockManager blockManager = resource.getBlockManager();
        Connection connection = resource.chooseConnection();
        Driver driver = resource.getDriver();
        driver.asyncGetTransaction(
                txHash,
                blockNumber,
                blockManager,
                connection,
                (exception, transaction) -> {
                    if (exception != null) {
                        logger.error("asyncGetVerifiedTransaction exception, ", exception);
                        callback.onReturn(
                                new WeCrossException(
                                        ErrorCode.HTLC_ERROR, "GET_VERIFIED_TRANSACTION_ERROR"),
                                false);
                    } else {
                        callback.onReturn(null, verifyData.verify(transaction));
                    }
                });
    }

    @Override
    public void getProposalInfo(
            Resource resource, Account account, String hash, Callback callback) {
        call(resource, account, "getProposalInfo", new String[] {hash}, callback);
    }

    @Override
    public HTLCProposal decodeProposal(String[] info) throws WeCrossException {
        if (info == null || info.length != 10) {
            throw new WeCrossException(
                    ErrorCode.HTLC_ERROR,
                    "DECODE_PROPOSAL_ERROR",
                    HTLCErrorCode.GET_PROPOSAL_INFO_ERROR,
                    "incomplete proposal");
        }

        HTLCProposal proposal = new HTLCProposal();
        proposal.setInitiator(RoutineDefault.TRUE_FLAG.equals(info[0]));
        proposal.setSecret(info[1]);
        proposal.setSelfTimelock(new BigInteger(info[2]));
        proposal.setSelfLocked(RoutineDefault.TRUE_FLAG.equals(info[3]));
        proposal.setSelfUnlocked(RoutineDefault.TRUE_FLAG.equals(info[4]));
        proposal.setSelfRolledback(RoutineDefault.TRUE_FLAG.equals(info[5]));
        proposal.setCounterpartyTimelock(new BigInteger(info[6]));
        proposal.setCounterpartyLocked(RoutineDefault.TRUE_FLAG.equals(info[7]));
        proposal.setCounterpartyUnlocked(RoutineDefault.TRUE_FLAG.equals(info[8]));
        proposal.setCounterpartyRolledback(RoutineDefault.TRUE_FLAG.equals(info[9]));

        return proposal;
    }

    @Override
    public void getCounterpartyHtlcAddress(Resource resource, Account account, Callback callback) {
        call(resource, account, "getCounterpartyHtlcAddress", null, callback);
    }

    @Override
    public void getProposalIDs(HTLCResource htlcResource, Callback callback) {
        call(htlcResource, "getProposalIDs", null, callback);
    }

    @Override
    public void deleteProposalID(HTLCResource htlcResource, String hash, Callback callback) {
        sendTransaction(htlcResource, "deleteProposalID", new String[] {hash}, callback);
    }

    @Override
    public void getNewProposalTxInfo(HTLCResource htlcResource, String hash, Callback callback) {
        call(htlcResource, "getNewProposalTxInfo", new String[] {hash}, callback);
    }

    @Override
    public void setCounterpartyLockState(
            HTLCResource htlcResource, String hash, Callback callback) {
        sendTransaction(htlcResource, "setCounterpartyLockState", new String[] {hash}, callback);
    }

    @Override
    public void setCounterpartyUnlockState(
            Resource resource, Account account, String hash, Callback callback) {
        sendTransaction(
                resource, account, "setCounterpartyUnlockState", new String[] {hash}, callback);
    }

    @Override
    public void setCounterpartyRollbackState(
            HTLCResource htlcResource, String hash, Callback callback) {
        sendTransaction(
                htlcResource, "setCounterpartyRollbackState", new String[] {hash}, callback);
    }

    private void call(
            Resource resource, Account account, String method, String[] args, Callback callback) {
        TransactionRequest request = new TransactionRequest(method, args);

        resource.asyncCall(
                request,
                account,
                newTransactionCallback(RoutineDefault.CALL_TYPE, method, callback));
    }

    private void call(HTLCResource htlcResource, String method, String[] args, Callback callback) {
        TransactionRequest request = new TransactionRequest(method, args);

        htlcResource.asyncCall(
                request,
                htlcResource.getAccount1(),
                newTransactionCallback(RoutineDefault.CALL_TYPE, method, callback));
    }

    private void sendTransaction(
            Resource resource, Account account, String method, String[] args, Callback callback) {
        TransactionRequest request = new TransactionRequest(method, args);

        resource.asyncSendTransaction(
                request,
                account,
                newTransactionCallback(RoutineDefault.SEND_TRANSACTION_TYPE, method, callback));
    }

    private void sendTransaction(
            HTLCResource htlcResource, String method, String[] args, Callback callback) {
        TransactionRequest request = new TransactionRequest(method, args);
        htlcResource.asyncSendTransaction(
                request,
                htlcResource.getAccount1(),
                newTransactionCallback(RoutineDefault.SEND_TRANSACTION_TYPE, method, callback));
    }

    private Resource.Callback newTransactionCallback(
            String type, String method, Callback callback) {
        return new Resource.Callback() {
            @Override
            public void onTransactionResponse(
                    TransactionException transactionException,
                    TransactionResponse transactionResponse) {
                if (transactionException != null && !transactionException.isSuccess()) {
                    logger.error(
                            "{}, method: {}, errorCode: {}, errorMsg: {}",
                            type,
                            method,
                            transactionException.getErrorCode(),
                            transactionException.getMessage());
                    callback.onReturn(
                            new WeCrossException(
                                    ErrorCode.HTLC_ERROR,
                                    method.toUpperCase() + "_ERROR",
                                    HTLCErrorCode.TRANSACTION_ERROR,
                                    transactionException.getMessage()),
                            null);
                } else {
                    try {
                        String result =
                                handleTransactionResponse(type, method, transactionResponse);
                        callback.onReturn(null, result);
                    } catch (WeCrossException e) {
                        callback.onReturn(e, null);
                    }
                }
            }
        };
    }

    private String handleTransactionResponse(
            String type, String method, TransactionResponse response) throws WeCrossException {
        if (response.getErrorCode() != 0) {
            logger.error(
                    "{}, method: {}, errorCode: {}, errorMsg: {}",
                    type,
                    method,
                    response.getErrorCode(),
                    response.getErrorMessage());

            throw new WeCrossException(
                    ErrorCode.HTLC_ERROR,
                    method.toUpperCase() + "_ERROR",
                    HTLCErrorCode.TRANSACTION_ERROR,
                    response.getErrorMessage());
        }

        if (logger.isDebugEnabled()) {
            logger.debug("{} response: {}", type, response);
        }

        String[] result = response.getResult();
        if (result == null || result.length == 0) {
            return null;
        } else {
            return result[0].trim();
        }
    }
}
