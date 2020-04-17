package com.webank.wecross.remote;

import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.wecross.p2p.P2PMessageCallback;
import com.webank.wecross.p2p.engine.P2PResponse;
import com.webank.wecross.stub.Response;
import com.webank.wecross.stub.StubQueryStatus;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RemoteConnectionSemaphoreCallback extends P2PMessageCallback {
    private Logger logger = LoggerFactory.getLogger(RemoteConnectionSemaphoreCallback.class);

    public transient Semaphore semaphore = new Semaphore(1, true);
    private Response responseData;

    public RemoteConnectionSemaphoreCallback() {
        super.setTypeReference(new TypeReference<P2PResponse<Response>>() {});
        try {
            semaphore.acquire(1);

        } catch (InterruptedException e) {
            logger.error("error: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void onResponse(int status, String message, P2PResponse msg) {
        responseData = (Response) msg.getData();
        semaphore.release();
    }

    public Response getResponseData() {
        try {
            semaphore.tryAcquire(1, 10, TimeUnit.SECONDS);

        } catch (InterruptedException e) {
            logger.error("Send error: {}", e);
            Response response = new Response();
            response.setErrorCode(StubQueryStatus.REMOTE_QUERY_FAILED);
            response.setErrorMessage("Send error: " + e.getMessage());
        }
        semaphore.release();
        return responseData;
    }
}
