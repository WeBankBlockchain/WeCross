package com.webank.wecross.p2p.engine.restful;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.P2PMessageCallback;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.Peer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

public class RestfulP2PMessageEngine extends P2PMessageEngine {
    private Logger logger = LoggerFactory.getLogger(RestfulP2PMessageEngine.class);

    private ThreadPoolTaskExecutor threadPool;

    private <T> void checkP2PMessage(P2PMessage<T> msg) throws Exception {
        if (msg.getVersion().isEmpty()) {
            throw new Exception("message version is empty");
        }
        if (msg.getMethod().isEmpty()) {
            throw new Exception("message method is empty");
        }
        if (msg.getSeq() == 0) {
            throw new Exception("message seq is 0");
        }
    }

    private void checkCallback(P2PMessageCallback callback) throws Exception {
        if (callback.getEngineCallbackMessageClassType() == null) {
            throw new Exception("callback getEngineCallbackMessageClassType has not set");
        }
        if (callback.getPeer() == null) {
            throw new Exception("callback from com.webank.wecross.peer has not set");
        }
    }

    private <T> void checkHttpResponse(ResponseEntity<P2PHttpResponse<T>> response)
            throws Exception {
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new Exception("Method not exists: " + response.toString());
        }
    }

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
                                ResponseEntity<P2PHttpResponse<Object>> response =
                                        restTemplate.exchange(
                                                url,
                                                HttpMethod.POST,
                                                request,
                                                callback.getEngineCallbackMessageClassType());

                                checkHttpResponse(response);
                                P2PHttpResponse<Object> responseMsg = response.getBody();
                                logger.debug(
                                        "Receive status:{} message:{} data:{}",
                                        responseMsg.getResult(),
                                        responseMsg.getMessage(),
                                        responseMsg.getData());

                                if (responseMsg.getData() != null) {

                                    logger.trace("trigger callback ", responseMsg.getData());

                                    ObjectMapper objMapper = new ObjectMapper();
                                    callback.setStatus(responseMsg.getResult());
                                    callback.setMessage(responseMsg.getMessage());
                                    callback.setData(
                                            responseMsg.toP2PMessage(
                                                    "restfulP2PMessageResponse")); // callback type
                                    // is the
                                    // same as msg
                                    callback.execute();
                                }
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
