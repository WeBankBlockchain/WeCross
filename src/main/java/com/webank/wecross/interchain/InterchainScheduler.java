package com.webank.wecross.interchain;

import static com.webank.wecross.interchain.InterchainDefault.TIMEOUT_DELAY;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.account.UniversalAccount;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.*;
import com.webank.wecross.utils.Sha256Utils;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterchainScheduler {
    private Logger logger = LoggerFactory.getLogger(InterchainScheduler.class);
    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    private SystemResource systemResource;
    private InterchainRequest interchainRequest;

    private UniversalAccount adminUA;
    private UniversalAccount userUA;

    public InterchainScheduler() {}

    public interface InterchainCallback {
        void onReturn(WeCrossException exception);
    }

    public void start(InterchainCallback callback) {
        getXATransactionState(
                (getTransactionStateException, xaTransactionID, xaTransactionSeq) -> {
                    if (Objects.nonNull(getTransactionStateException)) {
                        callback.onReturn(getTransactionStateException);
                        return;
                    }

                    if (logger.isDebugEnabled()) {
                        logger.debug(
                                "Transaction state, xaTransactionID: {}, xaTransactionSeq: {}, inter chain request: {}",
                                xaTransactionID,
                                xaTransactionSeq,
                                interchainRequest);
                    }

                    String realUid =
                            Sha256Utils.sha256String(
                                    (systemResource.getHubResource() + interchainRequest.getUid())
                                            .getBytes(StandardCharsets.UTF_8));

                    long timestamp = System.currentTimeMillis();
                    long callTargetChainSeq =
                            timestamp > xaTransactionSeq ? timestamp : (xaTransactionSeq + 1L);
                    callTargetChain(
                            realUid,
                            xaTransactionID,
                            callTargetChainSeq,
                            (callTargetChainException, callTargetChainResult) -> {
                                boolean state = true;
                                String result = callTargetChainResult;

                                if (Objects.nonNull(callTargetChainException)) {
                                    logger.error(
                                            "Call target chain failed, error code: {}, message: {}",
                                            callTargetChainException.getInternalErrorCode(),
                                            callTargetChainException.getInternalMessage());

                                    state = false;
                                    result = "[]";
                                }

                                if (logger.isDebugEnabled()) {
                                    logger.debug(
                                            "Call target chain, xaTransactionID: {}, xaTransactionSeq: {}, state: {}, result: {},  inter chain request: {}",
                                            xaTransactionID,
                                            callTargetChainSeq,
                                            state,
                                            result,
                                            interchainRequest);
                                }

                                boolean finalState = state;
                                String finalResult = result;
                                getXATransactionState(
                                        (getCallbackTransactionStateException,
                                                callbackXATransactionID,
                                                callbackXATransactionSeq) -> {
                                            if (Objects.nonNull(
                                                    getCallbackTransactionStateException)) {
                                                callback.onReturn(
                                                        getCallbackTransactionStateException);
                                                return;
                                            }

                                            long newTimestamp = System.currentTimeMillis();
                                            long callCallbackSeq =
                                                    newTimestamp > callbackXATransactionSeq
                                                            ? newTimestamp
                                                            : (callbackXATransactionSeq + 1L);

                                            callCallback(
                                                    Sha256Utils.sha256String(
                                                            realUid.getBytes(
                                                                    StandardCharsets.UTF_8)),
                                                    callbackXATransactionID,
                                                    callCallbackSeq,
                                                    finalState,
                                                    finalResult,
                                                    (callCallbackException,
                                                            errorCode,
                                                            message,
                                                            callCallbackResult) -> {
                                                        if (Objects.nonNull(
                                                                callCallbackException)) {
                                                            /* exception occurred, no need to register result */
                                                            callback.onReturn(
                                                                    callCallbackException);
                                                            return;
                                                        }

                                                        if (logger.isDebugEnabled()) {
                                                            logger.debug(
                                                                    "Call callback, xaTransactionID: {}, xaTransactionSeq: {}, result: {},  inter chain request: {}",
                                                                    callbackXATransactionID,
                                                                    callCallbackSeq,
                                                                    callCallbackResult,
                                                                    interchainRequest);
                                                        }

                                                        registerCallbackResult(
                                                                callbackXATransactionID,
                                                                callCallbackSeq,
                                                                errorCode,
                                                                message,
                                                                callCallbackResult,
                                                                registerCallbackResultException -> {
                                                                    if (logger.isDebugEnabled()) {
                                                                        logger.debug(
                                                                                "Register callback result, xaTransactionID: {}, xaTransactionSeq: {}, errorCode: {}, message: {}, result: {}, inter chain request: {}",
                                                                                callbackXATransactionID,
                                                                                callCallbackSeq,
                                                                                errorCode,
                                                                                message,
                                                                                callCallbackResult,
                                                                                interchainRequest);
                                                                    }
                                                                    callback.onReturn(
                                                                            registerCallbackResultException);
                                                                });
                                                    });
                                        },
                                        interchainRequest.getCallbackPath());
                            });
                },
                interchainRequest.getPath());
    }

    public interface GetTransactionStateCallback {
        void onReturn(WeCrossException exception, String xaTransactionID, long xaTransactionSeq);
    }

    public void getXATransactionState(GetTransactionStateCallback callback, String resourcePath) {
        Path path;
        try {
            path = Path.decode(resourcePath);
        } catch (Exception e) {
            callback.onReturn(
                    new WeCrossException(
                            WeCrossException.ErrorCode.INTER_CHAIN_ERROR,
                            "GET_XA_TRANSACTION_STATE_ERROR",
                            InterchainErrorCode.GET_XA_TRANSACTION_STATE_ERROR,
                            "Decode resource path error"),
                    null,
                    0);
            return;
        }
        path.setResource(StubConstant.PROXY_NAME);
        Path proxyPath = new Path(path);
        Resource proxyResource = systemResource.getZoneManager().fetchResource(proxyPath);

        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setArgs(new String[] {resourcePath});
        transactionRequest.setMethod(InterchainDefault.GET_XA_TRANSACTION_STATE_METHOD);
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
                                        "GET_XA_TRANSACTION_STATE_ERROR",
                                        InterchainErrorCode.GET_XA_TRANSACTION_STATE_ERROR,
                                        transactionException.getMessage()),
                                null,
                                0);
                    } else if (transactionResponse.getErrorCode() != 0) {
                        callback.onReturn(
                                new WeCrossException(
                                        WeCrossException.ErrorCode.INTER_CHAIN_ERROR,
                                        "GET_XA_TRANSACTION_STATE_ERROR",
                                        InterchainErrorCode.GET_XA_TRANSACTION_STATE_ERROR,
                                        transactionResponse.getMessage()),
                                null,
                                0);
                    } else {
                        if (Objects.isNull(transactionResponse.getResult())
                                || transactionResponse.getResult().length == 0) {
                            callback.onReturn(
                                    new WeCrossException(
                                            WeCrossException.ErrorCode.INTER_CHAIN_ERROR,
                                            "GET_XA_TRANSACTION_STATE_ERROR",
                                            InterchainErrorCode.GET_XA_TRANSACTION_STATE_ERROR,
                                            "No response found"),
                                    null,
                                    0);
                        } else {
                            String result = transactionResponse.getResult()[0].trim();
                            if (InterchainDefault.NULL_FLAG.equals(result)) {
                                callback.onReturn(null, null, 0);
                            } else {
                                /* state = "tid uid" */
                                String[] states = result.split(InterchainDefault.SPLIT_REGEX);
                                callback.onReturn(null, states[0], Long.parseLong(states[1]));
                            }
                        }
                    }
                });
    }

    public interface CallTargetChainCallback {
        // result is json form of string array
        void onReturn(WeCrossException exception, String result);
    }

    public void callTargetChain(
            String uid,
            String xaTransactionID,
            long xaTransactionSeq,
            CallTargetChainCallback callback) {
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
                        "[]");
                return;
            }

            TransactionRequest transactionRequest = new TransactionRequest();
            transactionRequest.setArgs(
                    new String[] {objectMapper.writeValueAsString(interchainRequest.getArgs())});
            transactionRequest.setMethod(interchainRequest.getMethod());

            transactionRequest.getOptions().put(StubConstant.TRANSACTION_UNIQUE_ID, uid);
            if (Objects.nonNull(xaTransactionID)
                    && !xaTransactionID.isEmpty()
                    && !InterchainDefault.NON_TRANSACTION.equals(xaTransactionID)) {
                transactionRequest
                        .getOptions()
                        .put(StubConstant.XA_TRANSACTION_ID, xaTransactionID);
                transactionRequest
                        .getOptions()
                        .put(StubConstant.XA_TRANSACTION_SEQ, xaTransactionSeq);
            }

            if (InterchainDefault.CALL_TYPE_INVOKE == interchainRequest.getCallType()) {

                Timer timer = systemResource.getTimer();
                Timeout callTargetChainTimeout =
                        timer.newTimeout(
                                timeout ->
                                        callback.onReturn(
                                                new WeCrossException(
                                                        WeCrossException.ErrorCode
                                                                .INTER_CHAIN_ERROR,
                                                        "CALL_TARGET_CHAIN_ERROR",
                                                        InterchainErrorCode.CALL_TARGET_CHAIN_ERROR,
                                                        "Timeout"),
                                                "[]"),
                                TIMEOUT_DELAY,
                                TimeUnit.SECONDS);

                resource.asyncSendTransaction(
                        transactionRequest,
                        userUA,
                        (transactionException, transactionResponse) -> {
                            // call back after timeout if call target failed
                            if (Objects.nonNull(transactionException)
                                    && !transactionException.isSuccess()) {
                                logger.error(
                                        "Call target chain failed, error code: {}, message: {}",
                                        transactionException.getErrorCode(),
                                        transactionException.getMessage());
                            } else if (transactionResponse.getErrorCode() != 0) {
                                logger.error(
                                        "Call target chain failed, error code: {}, message: {}",
                                        transactionResponse.getErrorCode(),
                                        transactionResponse.getMessage());
                            } else {
                                callTargetChainTimeout.cancel();
                                if (Objects.isNull(transactionResponse.getResult())
                                        || transactionResponse.getResult().length == 0) {
                                    callback.onReturn(null, "[]");
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
                    "[]");
        }
    }

    public interface CallCallbackCallback {
        // result is json form of string array
        void onReturn(WeCrossException exception, int errorCode, String message, String result);
    }

    public void callCallback(
            String uid,
            String xaTransactionID,
            long xaTransactionSeq,
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
                        "[]");
                return;
            }

            TransactionRequest transactionRequest = new TransactionRequest();
            transactionRequest.setArgs(new String[] {state.toString(), result});
            transactionRequest.setMethod(interchainRequest.getCallbackMethod());
            transactionRequest.getOptions().put(StubConstant.TRANSACTION_UNIQUE_ID, uid);
            if (Objects.nonNull(xaTransactionID)
                    && !xaTransactionID.isEmpty()
                    && !InterchainDefault.NON_TRANSACTION.equals(xaTransactionID)) {
                transactionRequest
                        .getOptions()
                        .put(StubConstant.XA_TRANSACTION_ID, xaTransactionID);
                transactionRequest
                        .getOptions()
                        .put(StubConstant.XA_TRANSACTION_SEQ, xaTransactionSeq);
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
                                    "[]");
                        } else if (transactionResponse.getErrorCode() != 0) {
                            callback.onReturn(
                                    null,
                                    transactionResponse.getErrorCode(),
                                    transactionResponse.getMessage(),
                                    "[]");
                        } else {
                            if (Objects.isNull(transactionResponse.getResult())
                                    || transactionResponse.getResult().length == 0) {
                                callback.onReturn(null, 0, "", "[]");
                            } else {
                                callback.onReturn(null, 0, "", transactionResponse.getResult()[0]);
                            }
                        }
                    });

        } catch (Exception e) {
            logger.error("Call callback exception, ", e);
            callback.onReturn(
                    new WeCrossException(
                            WeCrossException.ErrorCode.INTER_CHAIN_ERROR,
                            "CALL_CALLBACK_ERROR",
                            InterchainErrorCode.CALL_CALLBACK_ERROR,
                            "Exception occurred"),
                    0,
                    null,
                    "[]");
        }
    }

    public interface RegisterResultCallback {
        void onReturn(WeCrossException exception);
    }

    public void registerCallbackResult(
            String xaTransactionID,
            long xaTransactionSeq,
            int errorCode,
            String message,
            String result,
            RegisterResultCallback callback) {
        try {
            if (Objects.isNull(xaTransactionID)
                    || xaTransactionID.isEmpty()
                    || InterchainDefault.NON_TRANSACTION.equals(xaTransactionID)) {
                xaTransactionID = InterchainDefault.NON_TRANSACTION;
                xaTransactionSeq = 0;
            }

            if (errorCode == 0) {
                message = InterchainDefault.SUCCESS_FLAG;
            }

            Resource hubResource = systemResource.getHubResource();
            TransactionRequest transactionRequest = new TransactionRequest();
            transactionRequest.setArgs(
                    new String[] {
                        interchainRequest.getUid(),
                        xaTransactionID,
                        String.valueOf(xaTransactionSeq),
                        String.valueOf(errorCode),
                        message,
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
                                            transactionResponse.getMessage()));
                        } else {
                            callback.onReturn(null);
                        }
                    });
        } catch (Exception e) {
            logger.error("Register result of callback exception, ", e);
            callback.onReturn(
                    new WeCrossException(
                            WeCrossException.ErrorCode.INTER_CHAIN_ERROR,
                            "REGISTER_CALLBACK_RESULT_ERROR",
                            InterchainErrorCode.REGISTER_CALLBACK_RESULT_ERROR,
                            "Exception occurred"));
        }
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
