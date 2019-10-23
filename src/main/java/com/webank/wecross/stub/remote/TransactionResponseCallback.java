package com.webank.wecross.stub.remote;

import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.P2PMessageCallback;
import com.webank.wecross.p2p.engine.restful.P2PHttpResponse;
import com.webank.wecross.resource.response.TransactionResponse;
import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;

public class TransactionResponseCallback extends P2PMessageCallback<TransactionResponse> {

    public transient Semaphore semaphore = new Semaphore(1, true);
    private Logger logger = LoggerFactory.getLogger(TransactionResponseCallback.class);
    private TransactionResponse responseData;

    public TransactionResponseCallback() {
        super.setEngineCallbackMessageClassType(
                new ParameterizedTypeReference<P2PHttpResponse<TransactionResponse>>() {});
        try {
            semaphore.acquire(1);

        } catch (InterruptedException e) {
            logger.error("error:", e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void onResponse(int status, String message, P2PMessage msg) {
        responseData = (TransactionResponse) msg.getData();
        logger.info(
                "Receive transaction response. status: {} message: {} data: {}",
                status,
                message,
                responseData == null ? "null" : responseData);
        semaphore.release();
    }

    public TransactionResponse getResponseData() {
        return responseData;
    }
}
