package com.webank.wecross.routine.htlc;

public class HTLC {

    public static boolean lock(String ipath, String hash) {
        return false;
    }

    public static boolean unlock(String ipath, String hash) {
        return false;
    }

    public static boolean timeout(String ipath, String hash) {
        return false;
    }

    public static boolean verifyLock(String transactionHash) {
        return false;
    }

    public static boolean verifyUnlock(String transactionHash) {
        return false;
    }

    public static boolean verifyTimeout(String transactionHash) {
        return false;
    }
}
