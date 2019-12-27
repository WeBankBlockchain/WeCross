package com.webank.wecross.proof;

import com.webank.wecross.utils.WeCrossType;

public class ProofConfig {
    public static boolean supportSPV(String type) {
        switch (type) {
            case WeCrossType.TRANSACTION_RSP_TYPE_BCOS:
                return true;

            case WeCrossType.TRANSACTION_RSP_TYPE_JDCHAIN:
            case WeCrossType.TRANSACTION_RSP_TYPE_FABRIC:
            default:
                return false;
        }
    }
}
