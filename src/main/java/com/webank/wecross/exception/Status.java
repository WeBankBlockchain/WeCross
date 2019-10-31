package com.webank.wecross.exception;

public class Status {

    // common
    public static final int SUCCESS = 0;
    public static final int INTERNAL_ERROR = 1;

    // status in config
    public static final int FIELD_MISSING = 1001;
    public static final int UNEXPECTED_CONFIG = 1002;
    public static final int ILLEGAL_SYMBOL = 1003;

    // status in http
    public static final int VERSION_ERROR = 2001;
    public static final int PATH_ERROR = 2002;
    public static final int METHOD_ERROR = 2003;
}
