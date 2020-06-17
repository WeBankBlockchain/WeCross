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

        // exception in http
        public static final int VERSION_ERROR = 201;
        public static final int PATH_ERROR = 202;
        public static final int RESOURCE_ERROR = 203;
        public static final int METHOD_ERROR = 204;
        public static final int DECODE_TRANSACTION_REQUEST_ERROR = 205;
        public static final int UNSUPPORTED_TYPE = 206;

        // other
        public static final int HTLC_ERROR = 301;
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
