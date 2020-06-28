package com.webank.wecross.network.p2p;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.common.NetworkQueryStatus;
import com.webank.wecross.network.NetworkCallback;
import com.webank.wecross.network.NetworkMessage;
import com.webank.wecross.network.NetworkProcessor;
import com.webank.wecross.network.NetworkResponse;
import com.webank.wecross.network.NetworkService;
import com.webank.wecross.network.p2p.netty.MessageType;
import com.webank.wecross.network.p2p.netty.NettyService;
import com.webank.wecross.network.p2p.netty.RequestProcessor;
import com.webank.wecross.network.p2p.netty.request.Request;
import com.webank.wecross.network.p2p.netty.response.Response;
import com.webank.wecross.network.p2p.netty.response.ResponseCallBack;
import com.webank.wecross.network.p2p.netty.response.StatusCode;
import com.webank.wecross.peer.Peer;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class P2PService implements NetworkService {
    private Logger logger = LoggerFactory.getLogger(P2PService.class);
    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    private NettyService nettyService;

    @Override
    public <T> void asyncSendMessage(
            Peer peer, NetworkMessage<T> msg, NetworkCallback<?> callback) {

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
            request.setContent(objectMapper.writeValueAsString(msg));
        } catch (Exception e) {
            logger.error(" P2PMessage to json error: {}", e);
            executeCallback(callback, NetworkQueryStatus.INTERNAL_ERROR, e.getMessage(), null);
            return;
        }

        if (callback == null) {
            request.setTimeout(0);
        }
        // send request by p2p network
        nettyService.asyncSendRequest(
                peer.getNode(),
                request,
                new ResponseCallBack() {
                    @Override
                    public void onResponse(Response response) {
                        logger.trace(" receive response: {}", response);

                        if (callback != null) {
                            String content = response.getContent();
                            try {
                                /** send request failed or request transfer failed */
                                if (response.getErrorCode() != StatusCode.SUCCESS) {
                                    throw new IOException(response.getErrorMessage());
                                }

                                NetworkResponse<Object> networkResponse =
                                        callback.parseContent(content);
                                /** remote execute return not ok */
                                if (networkResponse.getErrorCode() != NetworkQueryStatus.SUCCESS) {
                                    throw new IOException(networkResponse.getMessage());
                                }

                                executeCallback(
                                        callback,
                                        NetworkQueryStatus.SUCCESS,
                                        NetworkQueryStatus.getStatusMessage(
                                                NetworkQueryStatus.SUCCESS),
                                        networkResponse);

                            } catch (Exception e) {
                                logger.warn("p2p error:", e);
                                executeCallback(
                                        callback,
                                        NetworkQueryStatus.INTERNAL_ERROR,
                                        e.getMessage(),
                                        null);
                            }
                        }
                    }

                    @Override
                    public boolean needOnResponse() {
                        return callback != null;
                    }
                });
    }

    @Override
    public void setNetworkProcessor(NetworkProcessor networkProcessor) {
        RequestProcessor nettyRequestProcessor =
                (RequestProcessor)
                        nettyService
                                .getChannelHandlerCallBack()
                                .getCallBack()
                                .getProcessor(MessageType.RESOURCE_REQUEST);
        nettyRequestProcessor.setNetworkProcessor(networkProcessor);
    }

    @Override
    public void start() throws Exception {
        nettyService.start();
    }

    public void setNettyService(NettyService nettyService) {
        this.nettyService = nettyService;
    }

    protected <T> void checkP2PMessage(NetworkMessage<T> msg) throws Exception {
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

    protected void checkCallback(NetworkCallback callback) throws Exception {
        if (callback != null) {}
    }

    //    protected <T> void checkHttpResponse(ResponseEntity<NetworkResponse<T>> response)
    //            throws Exception {
    //        if (response == null) {
    //            throw new Exception("Remote response null");
    //        }
    //        if (response.getStatusCode() != HttpStatus.OK) {
    //            throw new Exception("Method not exists: " + response.toString());
    //        }
    //    }

    protected void checkPeerResponse(NetworkResponse responseMsg) throws Exception {
        if (responseMsg == null) {
            throw new Exception("Peer response null");
        }
    }

    protected void executeCallback(
            NetworkCallback callback, int status, String message, NetworkResponse data) {
        if (callback != null) {
            callback.setStatus(status);
            callback.setMessage(message);
            callback.setData(data);
            callback.execute();
        }
    }
}
