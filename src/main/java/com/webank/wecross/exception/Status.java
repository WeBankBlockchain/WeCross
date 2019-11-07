package com.webank.wecross.exception;

public class Status {

    // common
    public static final int SUCCESS = 0;
    public static final int INTERNAL_ERROR = 1;
    public static final int FIELD_MISSING = 2;

    // status in config
    public static final int UNEXPECTED_CONFIG = 1001;
    public static final int ILLEGAL_SYMBOL = 1002;

    // status in http
    public static final int VERSION_ERROR = 2001;
    public static final int PATH_ERROR = 2002;
    public static final int METHOD_ERROR = 2003;
    public static final int RESOURCE_ERROR = 2004;

    public static final int JDCHAIN_CONNECTION_COUNRT_ERROR = 3001;
    public static final int JDCHAIN_GENERATE_CLASS_ERROR = 3002;
    public static final int JDCHAIN_INVOKE_METHOD_ERROR = 3003;
    public static final int JDCHAIN_COMMIT_ERROR = 3004;
    public static final int JDCHAIN_GENERATE_COMPILE_ERROR = 3005;
    public static final int JDCHAIN_METHOD_NOTSUPPORT = 3006;
    public static final int JDCHAIN_PARAMETER_INVALIDATE = 3007;
    public static final int JDCHAIN_GETDATA_ERROR = 3008;
}
