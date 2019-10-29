package com.webank.wecross.exception;

public class WeCrossException extends java.lang.Exception {

    private static final long serialVersionUID = 3754251447587995515L;

    /*
    0: no error
    1: internal error
    2: lack required fields in configuration
    3: unexpected info in configuration
    4: disallowed symbol in path
     */
    private int errorCode = 0;

    public WeCrossException(int code, String message) {
        super(message);
        errorCode = code;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
