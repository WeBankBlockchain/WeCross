package com.webank.wecross.stub.fabric;

import com.webank.wecross.config.ConfigInfo;
import com.webank.wecross.restserver.response.TransactionResponse;

public class FabricResponse extends TransactionResponse {

    public FabricResponse() {
        super.setType(ConfigInfo.TRANSACTION_RSP_TYPE_FABRIC);
    }
}
