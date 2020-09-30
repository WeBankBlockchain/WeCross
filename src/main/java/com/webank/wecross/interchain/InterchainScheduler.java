package com.webank.wecross.interchain;

import com.webank.wecross.account.UniversalAccount;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.*;
import com.webank.wecross.utils.Sha256Utils;
import java.nio.charset.StandardCharsets;
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

                    String realUid =
                            Sha256Utils.sha256String(
                                    (systemResource.getHubResource() + interchainRequest.getUid())
                                            .getBytes(StandardCharsets.UTF_8));
                    callTargetChain(
                            realUid,
                            tid,
                            seq + 1,
                            (callTargetChainException, callTargetChainResult) -> {
                                boolean state = true;
                                String result = callTargetChainResult;

                                if (Objects.nonNull(callTargetChainException)) {
                                    logger.error(
                                            "Call target chain failed, error code: {}, message: {}",
                                            callTargetChainException.getInternalErrorCode(),
                                            callTargetChainException.getInternalMessage());

                                    state = false;
                                    result = "";
                                }

                                if (logger.isDebugEnabled()) {
                                    logger.debug(
                                            "Call target chain, tid: {}, seq: {}, state: {}, result: {},  inter chain request: {}",
                                            tid,
                                            seq + 1,
                                            state,
                                            result,
                                            interchainRequest);
                                }

                                callCallback(
                                        Sha256Utils.sha256String(
                                                realUid.getBytes(StandardCharsets.UTF_8)),
                                        tid,
                                        seq + 2,
                                        state,
                                        result,
                                        (callCallbackException,
                                                errorCode,
                                                errorMsg,
                                                callCallbackResult) -> {
                                            if (Objects.nonNull(callCallbackException)) {
                                                /* exception occurred, no need to register result */
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
                                                        callback.onReturn(
                                                                registerCallbackResultException);
                                                    });
                                        });
                            });
                });
    }

    public interface GetTransactionStateCallback {
        void onReturn(WeCrossException exception, String tid, int seq);
    }

    public void getTransactionState(GetTransactionStateCallback callback) {
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
                            String result = transactionResponse.getResult()[0];
                            if (InterchainDefault.NULL_FLAG.equals(result.trim())) {
                                callback.onReturn(null, null, 0);
                            } else {
                                /* state = "tid uid" */
                                String[] states =
                                        transactionResponse.getResult()[0].split(
                                                InterchainDefault.SPLIT_REGEX);
                                callback.onReturn(null, states[0], Integer.parseInt(states[1]));
                            }
                        }
                    }
                });
    }

    public interface CallTargetChainCallback {
        void onReturn(WeCrossException exception, String result);
    }

    public void callTargetChain(String uid, String tid, int seq, CallTargetChainCallback callback) {
        try {
            Path path = Path.decode(interchainRequest.getPath());
            Resource resource = systemResource.getZoneManager().fetchResource(path);
            if (Objects.isNull(resource)) {
                callback.onReturn(
                        new WeCrossException(
                                WeCrossException.ErrorCode.INTER_CHAIN_ERROR,
                                "CALL_TARGET_CHAIN_ERROR",
                                InterchainErrorCode.CALL_TARGET_CHAIN_ERROR,
                                "Target path '" + path + "' not found"),
                        null);
                return;
            }

            TransactionRequest transactionRequest = new TransactionRequest();
            transactionRequest.setArgs(interchainRequest.getArgs());
            transactionRequest.setMethod(interchainRequest.getMethod());

            transactionRequest.getOptions().put(StubConstant.TRANSACTION_UNIQUE_ID, uid);
            if (Objects.nonNull(tid)
                    && !tid.isEmpty()
                    && !InterchainDefault.NON_TRANSACTION.equals(tid)) {
                transactionRequest.getOptions().put(StubConstant.TRANSACTION_ID, tid);
                transactionRequest
                        .getOptions()
                        .put(StubConstant.TRANSACTION_SEQ, String.valueOf(seq));
            }

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
                                    callback.onReturn(null, "");
                                } else {
                                    callback.onReturn(null, transactionResponse.getResult()[0]);
                                }
                            }
                        });
            }
        } catch (Exception e) {
            logger.error("Call target chain exception, ", e);
            callback.onReturn(
                    new WeCrossException(
                            WeCrossException.ErrorCode.INTER_CHAIN_ERROR,
                            "CALL_TARGET_CHAIN_ERROR",
                            InterchainErrorCode.CALL_TARGET_CHAIN_ERROR,
                            "Exception occurred"),
                    null);
        }
    }

    public interface CallCallbackCallback {
        void onReturn(WeCrossException exception, int errorCode, String errorMsg, String result);
    }

    public void callCallback(
            String uid,
            String tid,
            int seq,
            Boolean state,
            String result,
            CallCallbackCallback callback) {
        try {
            Path path = Path.decode(interchainRequest.getCallbackPath());
            Resource resource = systemResource.getZoneManager().fetchResource(path);
            if (Objects.isNull(resource)) {
                callback.onReturn(
                        new WeCrossException(
                                WeCrossException.ErrorCode.INTER_CHAIN_ERROR,
                                "CALL_CALLBACK_ERROR",
                                InterchainErrorCode.CALL_CALLBACK_ERROR,
                                "Callback path '" + path + "' not found"),
                        0,
                        null,
                        null);
                return;
            }

            TransactionRequest transactionRequest = new TransactionRequest();
            transactionRequest.setArgs(new String[] {state.toString(), result});
            transactionRequest.setMethod(interchainRequest.getCallbackMethod());
            transactionRequest.getOptions().put(StubConstant.TRANSACTION_UNIQUE_ID, uid);
            if (Objects.nonNull(tid)
                    && !tid.isEmpty()
                    && !InterchainDefault.NON_TRANSACTION.equals(tid)) {
                transactionRequest.getOptions().put(StubConstant.TRANSACTION_ID, tid);
                transactionRequest
                        .getOptions()
                        .put(StubConstant.TRANSACTION_SEQ, String.valueOf(seq));
            }

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
                                    "");
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
            logger.error("Call call exception, ", e);
            callback.onReturn(
                    new WeCrossException(
                            WeCrossException.ErrorCode.INTER_CHAIN_ERROR,
                            "CALL_CALLBACK_ERROR",
                            InterchainErrorCode.CALL_CALLBACK_ERROR,
                            "Exception occurred"),
                    0,
                    null,
                    null);
        }
    }

    public interface RegisterResultCallback {
        void onReturn(WeCrossException exception);
    }

    public void registerCallbackResult(
            String tid,
            int seq,
            int errorCode,
            String errorMsg,
            String result,
            RegisterResultCallback callback) {

        if (Objects.isNull(tid) || tid.isEmpty() || InterchainDefault.NON_TRANSACTION.equals(tid)) {
            tid = InterchainDefault.NON_TRANSACTION;
            seq = 0;
        }

        if (errorCode == 0) {
            errorMsg = InterchainDefault.SUCCESS_FLAG;
        }

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
