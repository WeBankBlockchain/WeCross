package com.webank.wecross.p2p.engine;

import com.webank.wecross.exception.Status;
import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.P2PMessageCallback;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.netty.common.Peer;
import com.webank.wecross.p2p.netty.message.MessageType;
import com.webank.wecross.p2p.netty.request.Request;
import com.webank.wecross.p2p.netty.response.Response;
import com.webank.wecross.p2p.netty.response.ResponseCallBack;
import com.webank.wecross.p2p.netty.response.StatusCode;
import java.io.IOException;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestfulP2PMessageEngine extends P2PMessageEngine {
    private Logger logger = LoggerFactory.getLogger(RestfulP2PMessageEngine.class);

    @Override
    public <T> void asyncSendMessage(Peer peer, P2PMessage<T> msg, P2PMessageCallback callback) {

        // check parameters
        try {
            checkP2PMessage(msg);
            checkCallback(callback);
        } catch (Exception e) {
            logger.error("asyncSendMessage error: {}", e);
            executeCallback(callback, -1, e.getMessage(), null);
            return;
        }

        // build request object
        Request request = new Request();
        request.setType(MessageType.RESOURCE_REQUEST);
        try {
            request.setContent(ObjectMapperFactory.getObjectMapper().writeValueAsString(msg));
        } catch (Exception e) {
            logger.error(" P2PMessage to json error: {}", e);
            executeCallback(callback, Status.INTERNAL_ERROR, e.getMessage(), null);
            return;
        }

        // send request by p2p network
        getP2PService()
                .asyncSendRequest(
                        peer,
                        request,
                        new ResponseCallBack() {
                            @Override
                            public void onResponse(Response response) {

                                logger.info(" receive response: {}", response);

                                String content = response.getContent();
                                try {
                                    /** send request failed or request transfer failed */
                                    if (response.getErrorCode() != StatusCode.SUCCESS) {
                                        throw new IOException(response.getErrorMessage());
                                    }

                                    P2PResponse<Object> p2PResponse =
                                            callback.parseContent(content);
                                    /** remote execute return not ok */
                                    if (p2PResponse.getResult() != Status.SUCCESS) {
                                        throw new IOException(p2PResponse.getMessage());
                                    }

                                    executeCallback(
                                            callback,
                                            Status.SUCCESS,
                                            response.getErrorMessage(),
                                            p2PResponse.toP2PMessage("restfulP2PMessageResponse"));

                                } catch (Exception e) {
                                    executeCallback(
                                            callback, Status.INTERNAL_ERROR, e.getMessage(), null);
                                    logger.error(" error : {}", e.getMessage());
                                }
                            }
                        });
    }
}
