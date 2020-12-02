package com.webank.wecross.routine;

public class RoutineDefault {

    public static final String SUCCESS_FLAG = "success";
    public static final String SPLIT_REGEX = "##";
    public static final String NULL_FLAG = "null";
    public static final String TRUE_FLAG = "true";
    public static final String FALSE_FLAG = "false";
    public static final String NOT_YET = "not_yet";
    public static final String CALL_TYPE = "call";
    public static final String SEND_TRANSACTION_TYPE = "sendTransaction";
    public static final String LOCK_METHOD = "lock";
    public static final String UNLOCK_METHOD = "unlock";
    public static final String ROLLBACK_METHOD = "rollback";
    public static final String HTLC_JOB_DATA_KEY = "htlc_job";

    // milli seconds
    public static final int POLLING_CYCLE = 1000;
    public static final long CALLBACK_TIMEOUT = 30000;
}
