package com.webank.wecross.stub.jdchain;

import com.webank.wecross.restserver.response.TransactionResponse;
import com.webank.wecross.utils.WeCrossType;

public class JDChainResponse extends TransactionResponse {
    public JDChainResponse() {
        super.setType(WeCrossType.TRANSACTION_RSP_TYPE_JDCHAIN);
    }
}
