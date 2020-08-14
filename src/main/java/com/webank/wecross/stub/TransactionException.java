package com.webank.wecross.stub;

public class TransactionException extends java.lang.Exception {

    public static class ErrorCode {
        public static final int SUCCESS = 0;

        public static final int REMOTE_QUERY_FAILED = StubQueryStatus.REMOTE_QUERY_FAILED; // 100

        public static final int TIMEOUT = StubQueryStatus.TIMEOUT; // 200

        public static final int INTERNAL_ERROR = 1001;

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
            return new TransactionException(ErrorCode.SUCCESS, "Success");
        }

        public static TransactionException newInternalException(String message) {
            return new TransactionException(ErrorCode.INTERNAL_ERROR, message);
        }
    }
}
