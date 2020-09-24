package com.webank.wecross.interchain;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.*;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterchainScheduler {
    private Logger logger = LoggerFactory.getLogger(InterchainScheduler.class);

    private SystemResource systemResource;
    private InterchainRequest interchainRequest;

    private UniversalAccount adminUA;
    private UniversalAccount userUA;

    public InterchainScheduler() {}

    public interface InterchainCallback {
        void onReturn(WeCrossException exception);
    }

    public void start(InterchainCallback callback) {
        getTransactionState(
                (getTransactionStateException, tid, seq) -> {
                    if (Objects.nonNull(getTransactionStateException)) {
                        callback.onReturn(getTransactionStateException);
                        return;
                    }

                    if (logger.isDebugEnabled()) {
                        logger.debug(
                                "Transaction state, tid: {}, seq: {}, inter chain request: {}",
                                tid,
                                seq,
                                interchainRequest);
                    }

                    // todo parse uid
                    callTargetChain(
                            tid,
                            seq + 1,
                            (callTargetChainException, callTargetChainResult) -> {
                                if (Objects.nonNull(callTargetChainException)) {
                                    callback.onReturn(callTargetChainException);
                                    return;
                                }

                                if (logger.isDebugEnabled()) {
                                    logger.debug(
                                            "Call target chain, tid: {}, seq: {}, result: {},  inter chain request: {}",
                                            tid,
                                            seq + 1,
                                            callTargetChainResult,
                                            interchainRequest);
                                }

                                callCallback(
                                        tid,
                                        seq + 2,
                                        callTargetChainResult,
                                        (callCallbackException,
                                                errorCode,
                                                errorMsg,
                                                callCallbackResult) -> {
                                            if (Objects.nonNull(callCallbackException)) {
                                                callback.onReturn(callCallbackException);
                                                return;
                                            }

                                            if (logger.isDebugEnabled()) {
                                                logger.debug(
                                                        "Call callback, tid: {}, seq: {}, result: {},  inter chain request: {}",
                                                        tid,
                                                        seq + 2,
                                                        callCallbackResult,
                                                        interchainRequest);
                                            }

                                            registerCallbackResult(
                                                    tid,
                                                    seq + 2,
                                                    errorCode,
                                                    errorMsg,
                                                    callCallbackResult,
                                                    registerCallbackResultException -> {
                                                        if (Objects.nonNull(
                                                                registerCallbackResultException)) {
                                                            callback.onReturn(
                                                                    registerCallbackResultException);
                                                            return;
                                                        }

                                                        if (logger.isDebugEnabled()) {
                                                            logger.debug(
                                                                    "Register callback result, tid: {}, seq: {}, errorCode: {}, errorMsg: {}, result: {}, inter chain request: {}",
                                                                    tid,
                                                                    seq + 2,
                                                                    errorCode,
                                                                    errorMsg,
                                                                    callCallbackResult,
                                                                    interchainRequest);
                                                        }
                                                    });
                                        });
                            });
                });
    }

    private interface GetTransactionStateCallback {
        void onReturn(WeCrossException exception, String tid, int seq);
    }

    private void getTransactionState(GetTransactionStateCallback callback) {
        Resource proxyResource = systemResource.getProxyResource();
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setArgs(new String[] {interchainRequest.getPath()});
        transactionRequest.setMethod(InterchainDefault.GET_TRANSACTION_STATE_METHOD);
        transactionRequest.getOptions().put(Resource.RAW_TRANSACTION, true);

        proxyResource.asyncCall(
                transactionRequest,
                adminUA,
                (transactionException, transactionResponse) -> {
                    if (Objects.nonNull(transactionException)
                            && !transactionException.isSuccess()) {
                        callback.onReturn(
                                new WeCrossException(
                                        WeCrossException.ErrorCode.INTER_CHAIN_ERROR,
                                        "GET_TRANSACTION_STATE_ERROR",
                                        InterchainErrorCode.GET_TRANSACTION_STATE_ERROR,
                                        transactionException.getMessage()),
                                null,
                                0);
                    } else if (transactionResponse.getErrorCode() != 0) {
                        callback.onReturn(
                                new WeCrossException(
                                        WeCrossException.ErrorCode.INTER_CHAIN_ERROR,
                                        "GET_TRANSACTION_STATE_ERROR",
                                        InterchainErrorCode.GET_TRANSACTION_STATE_ERROR,
                                        transactionResponse.getErrorMessage()),
                                null,
                                0);
                    } else {
                        if (Objects.isNull(transactionResponse.getResult())
                                || transactionResponse.getResult().length == 0) {
                            callback.onReturn(
                                    new WeCrossException(
                                            WeCrossException.ErrorCode.INTER_CHAIN_ERROR,
                                            "GET_TRANSACTION_STATE_ERROR",
                                            InterchainErrorCode.GET_TRANSACTION_STATE_ERROR,
                                            "No response found"),
                                    null,
                                    0);
                        } else {
                            /* result = "tid uid" */
                            String[] tempRes =
                                    transactionResponse.getResult()[0].split(
                                            InterchainDefault.SPLIT_REGEX);
                            callback.onReturn(null, tempRes[0], Integer.parseInt(tempRes[1]));
                        }
                    }
                });
    }

    private interface CallTargetChainCallback {
        void onReturn(WeCrossException exception, String result);
    }

    private void callTargetChain(String tid, int seq, CallTargetChainCallback callback) {
        try {
            Path path = Path.decode(interchainRequest.getPath());
            Resource resource = systemResource.getZoneManager().fetchResource(path);

            TransactionRequest transactionRequest = new TransactionRequest();
            transactionRequest.setArgs(interchainRequest.getArgs());
            transactionRequest.setMethod(interchainRequest.getMethod());
            transactionRequest.getOptions().put(StubConstant.TRANSACTION_ID, tid);
            transactionRequest.getOptions().put(StubConstant.TRANSACTION_SEQ, String.valueOf(seq));

            if (InterchainDefault.CALL_TYPE_INVOKE == interchainRequest.getCallType()) {
                resource.asyncSendTransaction(
                        transactionRequest,
                        userUA,
                        (transactionException, transactionResponse) -> {
                            if (Objects.nonNull(transactionException)
                                    && !transactionException.isSuccess()) {
                                callback.onReturn(
                                        new WeCrossException(
                                                WeCrossException.ErrorCode.INTER_CHAIN_ERROR,
                                                "CALL_TARGET_CHAIN_ERROR",
                                                InterchainErrorCode.CALL_TARGET_CHAIN_ERROR,
                                                transactionException.getMessage()),
                                        null);
                            } else if (transactionResponse.getErrorCode() != 0) {
                                callback.onReturn(
                                        new WeCrossException(
                                                WeCrossException.ErrorCode.INTER_CHAIN_ERROR,
                                                "CALL_TARGET_CHAIN_ERROR",
                                                InterchainErrorCode.CALL_TARGET_CHAIN_ERROR,
                                                transactionResponse.getErrorMessage()),
                                        null);
                            } else {
                                if (Objects.isNull(transactionResponse.getResult())
                                        || transactionResponse.getResult().length == 0) {
                                    callback.onReturn(null, null);
                                } else {
                                    callback.onReturn(null, transactionResponse.getResult()[0]);
                                }
                            }
                        });
            }
        } catch (Exception e) {
            callback.onReturn(
                    new WeCrossException(
                            WeCrossException.ErrorCode.INTER_CHAIN_ERROR,
                            "CALL_TARGET_CHAIN_ERROR",
                            InterchainErrorCode.CALL_TARGET_CHAIN_ERROR,
                            "Exception occured"),
                    null);
        }
    }

    private interface CallCallbackCallback {
        void onReturn(WeCrossException exception, int errorCode, String errorMsg, String result);
    }

    private void callCallback(String tid, int seq, String args, CallCallbackCallback callback) {
        try {
            Path path = Path.decode(interchainRequest.getCallbackPath());
            Resource resource = systemResource.getZoneManager().fetchResource(path);

            TransactionRequest transactionRequest = new TransactionRequest();
            transactionRequest.setArgs(new String[] {args});
            transactionRequest.setMethod(interchainRequest.getCallbackMethod());
            transactionRequest.getOptions().put(StubConstant.TRANSACTION_ID, tid);
            transactionRequest.getOptions().put(StubConstant.TRANSACTION_SEQ, String.valueOf(seq));

            resource.asyncSendTransaction(
                    transactionRequest,
                    userUA,
                    (transactionException, transactionResponse) -> {
                        if (Objects.nonNull(transactionException)
                                && !transactionException.isSuccess()) {
                            callback.onReturn(
                                    new WeCrossException(
                                            WeCrossException.ErrorCode.INTER_CHAIN_ERROR,
                                            "CALL_CALLBACK_ERROR",
                                            InterchainErrorCode.CALL_CALLBACK_ERROR,
                                            transactionException.getMessage()),
                                    0,
                                    null,
                                    null);
                        } else if (transactionResponse.getErrorCode() != 0) {
                            callback.onReturn(
                                    null,
                                    transactionResponse.getErrorCode(),
                                    transactionResponse.getErrorMessage(),
                                    null);
                        } else {
                            if (Objects.isNull(transactionResponse.getResult())
                                    || transactionResponse.getResult().length == 0) {
                                callback.onReturn(null, 0, "", "");
                            } else {
                                callback.onReturn(null, 0, "", transactionResponse.getResult()[0]);
                            }
                        }
                    });

        } catch (Exception e) {
            callback.onReturn(
                    new WeCrossException(
                            WeCrossException.ErrorCode.INTER_CHAIN_ERROR,
                            "CALL_CALLBACK_ERROR",
                            InterchainErrorCode.CALL_CALLBACK_ERROR,
                            "Exception occured"),
                    0,
                    null,
                    null);
        }
    }

    private interface RegisterResultCallback {
        void onReturn(WeCrossException exception);
    }

    private void registerCallbackResult(
            String tid,
            int seq,
            int errorCode,
            String errorMsg,
            String result,
            RegisterResultCallback callback) {

        Resource hubResource = systemResource.getHubResource();
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setArgs(
                new String[] {
                    interchainRequest.getUid(),
                    tid,
                    String.valueOf(seq),
                    String.valueOf(errorCode),
                    errorMsg,
                    result
                });
        transactionRequest.setMethod(InterchainDefault.REGISTER_CALLBACK_RESULT_METHOD);

        hubResource.asyncSendTransaction(
                transactionRequest,
                adminUA,
                (transactionException, transactionResponse) -> {
                    if (Objects.nonNull(transactionException)
                            && !transactionException.isSuccess()) {
                        callback.onReturn(
                                new WeCrossException(
                                        WeCrossException.ErrorCode.INTER_CHAIN_ERROR,
                                        "REGISTER_CALLBACK_RESULT_ERROR",
                                        InterchainErrorCode.REGISTER_CALLBACK_RESULT_ERROR,
                                        transactionException.getMessage()));
                    } else if (transactionResponse.getErrorCode() != 0) {
                        callback.onReturn(
                                new WeCrossException(
                                        WeCrossException.ErrorCode.INTER_CHAIN_ERROR,
                                        "REGISTER_CALLBACK_RESULT_ERROR",
                                        InterchainErrorCode.REGISTER_CALLBACK_RESULT_ERROR,
                                        transactionResponse.getErrorMessage()));
                    } else {
                        callback.onReturn(null);
                    }
                });
    }

    public SystemResource getSystemResource() {
        return systemResource;
    }

    public void setSystemResource(SystemResource systemResource) {
        this.systemResource = systemResource;
    }

    public InterchainRequest getInterchainRequest() {
        return interchainRequest;
    }

    public void setInterchainRequest(InterchainRequest interchainRequest) {
        this.interchainRequest = interchainRequest;
    }

    public UniversalAccount getAdminUA() {
        return adminUA;
    }

    public void setAdminUA(UniversalAccount adminUA) {
        this.adminUA = adminUA;
    }

    public UniversalAccount getUserUA() {
        return userUA;
    }

    public void setUserUA(UniversalAccount userUA) {
        this.userUA = userUA;
    }
}
