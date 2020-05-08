package com.webank.wecross.common;

public class NetworkQueryStatus {

    // The errorCode definition of RestResponse and P2PResponse

    public static final int SUCCESS = 0;

    // 10100+ is reserved for ErrorCode in WeCrossException
    // Real_State = EXCEPTION_FLAG + ErrorCode
    public static final int EXCEPTION_FLAG = 10000;

    public static final int INTERNAL_ERROR = 20000;
    public static final int METHOD_ERROR = 20001;

    public static String getStatusMessage(int status) {
        return getStatusMessage(status, "Error code: " + status);
    }

    public static String getStatusMessage(int status, String errorMessage) {
        String message;
        switch (status) {
            case SUCCESS:
                message = "Success";
                break;
            default:
                message = errorMessage;
                break;
        }
        return message;
    }
}
