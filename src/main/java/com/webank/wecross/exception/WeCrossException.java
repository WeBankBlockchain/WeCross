package com.webank.wecross.exception;

public class WeCrossException extends java.lang.Exception {
    private static final long serialVersionUID = 3754251447587995515L;

    private int errorCode = 0;

    public WeCrossException(int code, String message) {
        super(message);
        errorCode = code;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
