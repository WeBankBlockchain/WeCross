package com.webank.wecross.stub.jdchain;

import com.webank.wecross.config.ConfigInfo;
import com.webank.wecross.restserver.response.TransactionResponse;

public class JDChainResponse extends TransactionResponse {
    public JDChainResponse() {
        super.setType(ConfigInfo.TRANSACTION_RSP_TYPE_JDCHAIN);
    }
}
