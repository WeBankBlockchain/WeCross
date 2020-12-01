package com.webank.wecross.common;

public class NetworkQueryStatus {

    // The errorCode definition of RestResponse and P2PResponse

    public static final int SUCCESS = 0;

    public static final int INTERNAL_ERROR = 10000;
    public static final int URI_PATH_ERROR = 10001;
    public static final int URI_QUERY_ERROR = 10002;

    /** 20000+ is reserved for network package real_state = 20000 + WeCrossException.ErrorCode */
    public static final int NETWORK_PACKAGE_ERROR = 20000;

    /** 30000+ is universal account error real_state = 30000 + WeCrossException.ErrorCode */
    public static final int UNIVERSAL_ACCOUNT_ERROR = 30000;

    /**
     * 40000+ is resource error for sendTransaction, call real_state = 40000 +
     * TransactionException.ErrorCode
     */
    public static final int RESOURCE_ERROR = 40000;

    /**
     * 50000+ is resource error for listTransactions, getTransaction real_state = 50000 +
     * WeCrossException.ErrorCode
     */
    public static final int TRANSACTION_ERROR = 50000;

    /** 60000+ is XA error real_state = 60000 + WeCrossException.ErrorCode */
    public static final int XA_ERROR = 60000;

    /** 70000+ is HTLC error real_state = 70000 + HTLCErrorCode */
    public static final int HTLC_ERROR = 70000;

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
