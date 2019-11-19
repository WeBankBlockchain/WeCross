package com.webank.wecross.proof;

import com.webank.wecross.config.ConfigInfo;

public class ProofConfig {
    public static boolean supportSPV(String type) {
        switch (type) {
            case ConfigInfo.TRANSACTION_RSP_TYPE_BCOS:
                return true;

            case ConfigInfo.TRANSACTION_RSP_TYPE_JDCHAIN:
            case ConfigInfo.TRANSACTION_RSP_TYPE_FABRIC:
            default:
                return false;
        }
    }
}
