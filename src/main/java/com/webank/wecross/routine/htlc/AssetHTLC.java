package com.webank.wecross.routine.htlc;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.exception.WeCrossException.ErrorCode;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.routine.RoutineDefault;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.BlockHeaderManager;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionException;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import com.webank.wecross.stub.VerifiedTransaction;
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
    public void lockCounterparty(
            HTLCResource htlcResource, String address, String hash, Callback callback) {
        transferWithValidation(
                htlcResource, address, RoutineDefault.LOCK_METHOD, new String[] {hash}, callback);
    }

    @Override
    public void unlockCounterparty(
            HTLCResource htlcResource,
            String address,
            String hash,
            String secret,
            Callback callback) {
        transferWithValidation(
                htlcResource,
                address,
                RoutineDefault.UNLOCK_METHOD,
                new String[] {hash, secret},
                callback);
    }

    // lock or unlock with validation
    private void transferWithValidation(
            HTLCResource htlcResource,
            String address,
            String method,
            String[] args,
            Callback callback) {
        TransactionContext<TransactionRequest> request =
                packTransactionRequest(htlcResource, htlcResource.getAccount1(), method, args);
        htlcResource.asyncSendTransaction(
                request,
                new Resource.Callback() {
                    @Override
                    public void onTransactionResponse(
                            TransactionException transactionException,
                            TransactionResponse transactionResponse) {
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
                                        address,
                                        method,
                                        args,
                                        new String[] {RoutineDefault.SUCCESS_FLAG});

                        if (!verifyHtlcTransaction(htlcResource, verifyData)) {
                            callback.onReturn(
                                    new WeCrossException(
                                            ErrorCode.HTLC_ERROR,
                                            method.toUpperCase() + "_COUNTERPARTY_ERROR",
                                            HTLCErrorCode.VERIFY_TRANSACTION_ERROR,
                                            "failed to verify transaction"),
                                    null);
                        } else {
                            callback.onReturn(null, res);
                        }
                    }
                });
    }

    @Override
    public void rollback(HTLCResource htlcResource, String hash, Callback callback) {
        sendTransaction(
                htlcResource, RoutineDefault.ROLLBACK_METHOD, new String[] {hash}, callback);
    }

    @Override
    public boolean verifyHtlcTransaction(Resource resource, VerifyData verifyData) {
        String txHash = verifyData.getTransactionHash();
        long blockNumber = verifyData.getBlockNumber();
        BlockHeaderManager blockHeaderManager = resource.getResourceBlockHeaderManager();
        Connection connection = resource.chooseConnection();
        Driver driver = resource.getDriver();

        VerifiedTransaction verifiedTransaction =
                driver.getVerifiedTransaction(txHash, blockNumber, blockHeaderManager, connection);

        return verifyData.verify(verifiedTransaction);
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
        TransactionContext<TransactionRequest> request =
                packTransactionRequest(resource, account, method, args);
        resource.asyncCall(
                request, newTransactionCallback(RoutineDefault.CALL_TYPE, method, callback));
    }

    private void call(HTLCResource htlcResource, String method, String[] args, Callback callback) {
        TransactionContext<TransactionRequest> request =
                packTransactionRequest(htlcResource, htlcResource.getAccount1(), method, args);
        htlcResource.asyncCall(
                request, newTransactionCallback(RoutineDefault.CALL_TYPE, method, callback));
    }

    private void sendTransaction(
            Resource resource, Account account, String method, String[] args, Callback callback) {
        TransactionContext<TransactionRequest> request =
                packTransactionRequest(resource, account, method, args);
        resource.asyncSendTransaction(
                request,
                newTransactionCallback(RoutineDefault.SEND_TRANSACTION_TYPE, method, callback));
    }

    private void sendTransaction(
            HTLCResource htlcResource, String method, String[] args, Callback callback) {
        TransactionContext<TransactionRequest> request =
                packTransactionRequest(htlcResource, htlcResource.getAccount1(), method, args);
        htlcResource.asyncSendTransaction(
                request,
                newTransactionCallback(RoutineDefault.SEND_TRANSACTION_TYPE, method, callback));
    }

    private TransactionContext<TransactionRequest> packTransactionRequest(
            Resource resource, Account account, String method, String[] args) {
        TransactionRequest request = new TransactionRequest(method, args);
        TransactionContext<TransactionRequest> transactionContext =
                new TransactionContext<>(
                        request,
                        account,
                        resource.getResourceInfo(),
                        resource.getResourceBlockHeaderManager());
        logger.trace(
                "htlc request: {}, resource name: {}",
                request,
                resource.getResourceInfo().getName());
        return transactionContext;
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
        logger.trace("{} response: {}", type, response);
        String[] result = response.getResult();
        if (result == null || result.length == 0) {
            return null;
        } else {
            return result[0].trim();
        }
    }
}
