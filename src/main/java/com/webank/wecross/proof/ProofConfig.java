package com.webank.wecross.proof;

import com.webank.wecross.network.config.ConfigType;

public class ProofConfig {
    public static boolean supportSPV(String type) {
        switch (type) {
            case ConfigType.TRANSACTION_RSP_TYPE_BCOS:
                return true;

            case ConfigType.TRANSACTION_RSP_TYPE_JDCHAIN:
            case ConfigType.TRANSACTION_RSP_TYPE_FABRIC:
            default:
                return false;
        }
    }
}
