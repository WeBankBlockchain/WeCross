package com.webank.wecross.exception;

public class WeCrossException extends java.lang.Exception {

    private static final long serialVersionUID = 3754251447587995515L;

    private Integer errorCode;

    public WeCrossException(Integer code, String message) {
        super(message);
        errorCode = code;
    }

    public Integer getErrorCode() {
        return errorCode;
    }
}
