package com.webank.wecross.stub.jdchain;

import com.webank.wecross.network.config.ConfigType;
import com.webank.wecross.restserver.response.TransactionResponse;

public class JDChainResponse extends TransactionResponse {
    public JDChainResponse() {
        super.setType(ConfigType.TRANSACTION_RSP_TYPE_JDCHAIN);
    }
}
