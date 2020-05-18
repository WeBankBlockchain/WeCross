package com.webank.wecross.exception;

public class TransactionException extends java.lang.Exception {

    public static class ErrorCode {
        public static final int SUCCESS = 0;

        public static final int REMOTE_QUERY_FAILED = 100;
        public static final int TIMEOUT = 200;
        public static final int INTERNAL_ERROR = 900;

        // BCOS: 2000 - 2999

        // Fabric: 3000 - 3999
    }

    private static final long serialVersionUID = 3754251446587995515L;

    private Integer errorCode;

    public TransactionException(Integer errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public boolean isSuccess() {
        return getErrorCode() == ErrorCode.SUCCESS;
    }

    public static class Builder {
        public static TransactionException newSuccessException() {
            return new TransactionException(ErrorCode.SUCCESS, "Sucess");
        }

        public static TransactionException newInternalException(String message) {
            return new TransactionException(ErrorCode.INTERNAL_ERROR, message);
        }
    }
}
