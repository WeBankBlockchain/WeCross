package com.webank.wecross.common;

public class ResourceQueryStatus {

    public static final int SUCCESS = 0;
    public static final int INTERNAL_ERROR = 1000;
    public static final int UNSUPPORTED_TYPE = 1001;
    public static final int NONSENSE_CALL = 1002;

    // Fabric
    public static final int FABRIC_INVOKE_CHAINCODE_FAIL = 3001;
    public static final int FABRIC_COMMIT_CHAINCODE_FAIL = 3002;

    // JDChain
    public static final int JDCHAIN_CONNECTION_COUNRT_ERROR = 4001;
    public static final int JDCHAIN_GENERATE_CLASS_ERROR = 4002;
    public static final int JDCHAIN_INVOKE_METHOD_ERROR = 4003;
    public static final int JDCHAIN_COMMIT_ERROR = 4004;
    public static final int JDCHAIN_GENERATE_COMPILE_ERROR = 4005;
    public static final int JDCHAIN_PARAMETER_INVALIDATE = 4006;
    public static final int JDCHAIN_GETDATA_ERROR = 4007;
}
