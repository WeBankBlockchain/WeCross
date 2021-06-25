package com.webank.wecross.exception;

public class WeCrossException extends java.lang.Exception {

    public static class ErrorCode {

        public static final int INTERNAL_ERROR = 100;

        // configuration
        public static final int FIELD_MISSING = 101;
        public static final int UNEXPECTED_CONFIG = 102;
        public static final int ILLEGAL_SYMBOL = 103;
        public static final int DIR_NOT_EXISTS = 104;
        public static final int GET_CHAIN_CHECKSUM_ERROR = 105;

        // network package
        public static final int VERSION_ERROR = 201;
        public static final int METHOD_ERROR = 202;
        public static final int POST_DATA_ERROR = 203;
        public static final int PATH_FORMAT_ERROR = 204;

        // transaction
        public static final int CALL_CONTRACT_ERROR = 301;
        public static final int GET_BLOCK_NUMBER_ERROR = 302;
        public static final int GET_BLOCK_ERROR = 303;
        public static final int GET_TRANSACTION_ERROR = 304;

        // wecross application (is invisible to the external)
        public static final int HTLC_ERROR = 401;
        public static final int INTER_CHAIN_ERROR = 402;

        // account service
        public static final int ADMIN_LOGIN_FAILED = 501;
        public static final int GET_UA_FAILED = 502; // login expired
        public static final int GET_UA_BY_ACCOUNT_FAILED = 503;
        public static final int DIFFRENT_CHAIN_ACCOUNT_ID_TO_UA_ID = 504;
        public static final int UAPROOF_VERIFYIER_EXCEPTION = 505;
        public static final int PERMISSION_DENIED = 506;

        // client engine
        public static final int QUERY_TIMEOUT = 601;
        public static final int QUERY_PARAMS_ERROR = 602;
        public static final int QUERY_CLIENT_ERROR = 603;
        public static final int QUERY_SERVER_ERROR = 604;
        public static final int QUERY_CLIENT_INIT_ERROR = 605;

        // web service
        public static final int PAGE_NOT_FOUND = 701;
    }

    public static final int UNKNOWN_INTERNAL_ERROR = -6535;

    private static final long serialVersionUID = 3754251447587995515L;

    private Integer errorCode;

    private Integer internalErrorCode = UNKNOWN_INTERNAL_ERROR;

    private String internalMessage = "Unknown";

    public WeCrossException(Integer errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public WeCrossException(Integer errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public WeCrossException(
            Integer errorCode, String message, Integer internalErrorCode, String internalMessage) {
        super(message);
        this.errorCode = errorCode;
        this.internalErrorCode = internalErrorCode; // -100000 means unknown
        this.internalMessage = internalMessage;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public Integer getInternalErrorCode() {
        return internalErrorCode;
    }

    public String getInternalMessage() {
        return internalMessage;
    }
}
