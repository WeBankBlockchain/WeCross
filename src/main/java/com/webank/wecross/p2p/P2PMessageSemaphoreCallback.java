package com.webank.wecross.p2p;

import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class P2PMessageSemaphoreCallback<T> extends P2PMessageCallback<T> {
    private Logger logger = LoggerFactory.getLogger(P2PMessageSemaphoreCallback.class);
    public transient Semaphore semaphore = new Semaphore(1, true);
    private T responseData;

    public P2PMessageSemaphoreCallback() {
        try {
            semaphore.acquire(1);

        } catch (InterruptedException e) {
            logger.error("error:", e);
        }
    }

    @Override
    public void execute() {
        this.responseData = data.getData();
        semaphore.release();
        this.onResponse(status, message, data);
    }

    public T getResponseData() {
        return responseData;
    }
}
