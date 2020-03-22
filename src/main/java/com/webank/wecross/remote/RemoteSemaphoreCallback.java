package com.webank.wecross.remote;

import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.wecross.p2p.P2PMessageCallback;
import com.webank.wecross.p2p.engine.P2PResponse;
import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RemoteSemaphoreCallback extends P2PMessageCallback {
    private Logger logger = LoggerFactory.getLogger(RemoteSemaphoreCallback.class);

    public transient Semaphore semaphore = new Semaphore(1, true);
    private Object responseData;

    public RemoteSemaphoreCallback(TypeReference responseType) {
        super.setTypeReference(responseType);
        try {
            semaphore.acquire(1);

        } catch (InterruptedException e) {
            logger.error("error: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void onResponse(int status, String message, P2PResponse msg) {
        responseData = msg.getData();
        semaphore.release();
    }

    public Object getResponseData() {
        return responseData;
    }
}
