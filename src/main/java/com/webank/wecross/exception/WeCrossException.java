package com.webank.wecross.exception;

public class WeCrossException extends java.lang.Exception {

    public static class ErrorCode {

        public static final int INTERNAL_ERROR = 100;

        // exception in configuration
        public static final int FIELD_MISSING = 101;
        public static final int UNEXPECTED_CONFIG = 102;
        public static final int ILLEGAL_SYMBOL = 103;
        public static final int DIR_NOT_EXISTS = 104;
        public static final int REPEATED_KEY = 105;
        public static final int INVALID_ACCOUNT = 106;
        public static final int GET_CHAIN_CHECKSUM_ERROR = 107;

        // exception in http
        public static final int VERSION_ERROR = 201;
        public static final int PATH_ERROR = 202;
        public static final int RESOURCE_ERROR = 203;
        public static final int METHOD_ERROR = 204;
        public static final int DECODE_TRANSACTION_REQUEST_ERROR = 205;
        public static final int ACCOUNT_ERROR = 206;

        // other
        public static final int HTLC_ERROR = 301;
        public static final int INTER_CHAIN_ERROR = 302;

        // client engine
        public static final int QUERY_TIMEOUT = 401;
        public static final int QUERY_PARAMS_ERROR = 402;
        public static final int QUERY_CLIENT_ERROR = 403;
        public static final int QUERY_SERVER_ERROR = 404;
        public static final int QUERY_CLIENT_INIT_ERROR = 405;

        // account service
        public static final int ADMIN_LOGIN_FAILED = 501;
        public static final int GET_UA_FAILED = 502; // login expired
        public static final int GET_UA_BY_ACCOUNT_FAILED = 503;
        public static final int DIFFRENT_CHAIN_ACCOUNT_ID_TO_UA_ID = 504;
        public static final int UAPROOF_VERIFYIER_EXCEPTION = 505;

        // web service
        public static final int PAGE_NOT_FOUND = 601;
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
