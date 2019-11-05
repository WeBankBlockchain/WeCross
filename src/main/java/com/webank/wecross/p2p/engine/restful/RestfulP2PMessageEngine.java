package com.webank.wecross.p2p.engine.restful;

import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.P2PMessageCallback;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.Peer;
import com.webank.wecross.p2p.engine.P2PResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

public class RestfulP2PMessageEngine extends P2PMessageEngine {
    private Logger logger = LoggerFactory.getLogger(RestfulP2PMessageEngine.class);

    private ThreadPoolTaskExecutor threadPool;

    @Override
    public <T> void asyncSendMessage(Peer peer, P2PMessage<T> msg, P2PMessageCallback callback) {
        try {
            checkP2PMessage(msg);
            checkCallback(callback);

            String url = "http://" + peer.getUrl() + "/p2p/" + msg.getMethod();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<P2PMessage<T>> request = new HttpEntity<>(msg, headers);

            threadPool.execute(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                RestTemplate restTemplate = new RestTemplate();

                                @SuppressWarnings("unchecked")
                                ResponseEntity<P2PResponse<Object>> response =
                                        restTemplate.exchange(
                                                url,
                                                HttpMethod.POST,
                                                request,
                                                callback.getEngineCallbackMessageClassType());
                                checkHttpResponse(response);

                                P2PResponse<Object> responseMsg = response.getBody();
                                checkPeerResponse(responseMsg);
                                logger.debug(
                                        "Receive status:{} message:{} data:{}",
                                        responseMsg.getResult(),
                                        responseMsg.getMessage(),
                                        responseMsg.getData());

                                callback.setData(
                                        responseMsg.toP2PMessage(
                                                "restfulP2PMessageResponse")); // callback type
                                callback.setStatus(responseMsg.getResult());
                                callback.setMessage(responseMsg.getMessage());
                                callback.execute();

                            } catch (Exception e) {
                                logger.error(
                                        "asyncSendMessage failed, to:{}, exception:{}", url, e);
                                callback.setStatus(-1);
                                callback.setMessage(e.toString());
                                callback.setData(null);
                                callback.execute();
                            }
                        }
                    });
        } catch (Exception e) {
            logger.error("asyncSendMessage error", e);
            callback.setStatus(-1);
            callback.setMessage(e.toString());
            callback.setData(null);
            callback.execute();
        }
    }

    public void setThreadPool(ThreadPoolTaskExecutor threadPool) {
        this.threadPool = threadPool;
    }
}
