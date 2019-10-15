package com.webank.wecross.restserver.p2p;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.P2PMessageCallback;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.Peer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class RestfulP2PMessageEngine extends P2PMessageEngine {
    private Logger logger = LoggerFactory.getLogger(RestfulP2PMessageEngine.class);

    private <T> void checkP2PMessage(P2PMessage<T> msg) throws Exception {
        if (msg.getVersion().isEmpty()) {
            throw new Exception("message version is empty");
        }
        if (msg.getType().isEmpty()) {
            throw new Exception("message type is empty");
        }
        if (msg.getSeq() == 0) {
            throw new Exception("message seq is 0");
        }
    }

    private void checkCallback(P2PMessageCallback callback) throws Exception {
        if (callback.getEngineCallbackMessageClassType() == null) {
            throw new Exception("callback getEngineCallbackMessageClassType has not set");
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

            RestTemplate restTemplate = new RestTemplate();
            String url = peer.getUrl() + "/p2p/" + msg.toUri();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<P2PMessage<T>> request = new HttpEntity<>(msg, headers);
            ResponseEntity<P2PHttpResponse<Object>> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            request,
                            callback.getEngineCallbackMessageClassType());

            checkHttpResponse(response);
            P2PHttpResponse<Object> responseMsg = response.getBody();
            logger.info(
                    "Receive status:{} message:{} data:{}",
                    responseMsg.getResult(),
                    responseMsg.getMessage(),
                    responseMsg.getData());

            if (responseMsg.getData() != null) {

                logger.info("trigger callback ", responseMsg.getData());

                ObjectMapper objMapper = new ObjectMapper();
                callback.setStatus(responseMsg.getResult());
                callback.setMessage(responseMsg.getMessage());
                callback.setData(responseMsg.getData());
                callback.execute();
            }

        } catch (Exception e) {
            logger.error("asyncSendMessage error", e);
            callback.setStatus(-1);
            callback.setMessage(e.toString());
            callback.setData(null);
            callback.execute();
        }
    }
}
