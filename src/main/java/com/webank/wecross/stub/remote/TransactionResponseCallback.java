package com.webank.wecross.stub.remote;

import com.webank.wecross.p2p.P2PMessageSemaphoreCallback;
import com.webank.wecross.p2p.engine.restful.P2PHttpResponse;
import com.webank.wecross.resource.response.TransactionResponse;
import org.springframework.core.ParameterizedTypeReference;

public class TransactionResponseCallback extends P2PMessageSemaphoreCallback<TransactionResponse> {
    public TransactionResponseCallback() {
        super.setEngineCallbackMessageClassType(
                new ParameterizedTypeReference<P2PHttpResponse<TransactionResponse>>() {});
    }
}
