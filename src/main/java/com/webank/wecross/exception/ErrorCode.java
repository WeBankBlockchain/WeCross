package com.webank.wecross.exception;

public class ErrorCode {

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
    public static final int UNSUPPORTED_TYPE = 204;
    public static final int METHOD_ERROR = 205;
}
