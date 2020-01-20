package com.webank.wecross.stub.fabric;

import com.webank.wecross.common.ResourceQueryStatus;
import com.webank.wecross.common.WeCrossType;
import com.webank.wecross.restserver.response.TransactionResponse;

public class FabricResponse extends TransactionResponse {

    public FabricResponse() {
        super.setErrorCode(ResourceQueryStatus.SUCCESS);
        super.setType(WeCrossType.TRANSACTION_RSP_TYPE_FABRIC);
    }
}
