package com.webank.wecross.remote;

import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.wecross.network.NetworkCallback;
import com.webank.wecross.network.NetworkMessage;
import com.webank.wecross.network.NetworkResponse;
import com.webank.wecross.network.p2p.P2PService;
import com.webank.wecross.peer.Peer;
import com.webank.wecross.restserver.Versions;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.Response;
import com.webank.wecross.stub.StubQueryStatus;
import java.util.Map;

public class RemoteConnection implements Connection {
    private Peer peer;
    private String path;
    private P2PService p2PService;
    private Map<String, String> properties;
    private ConnectionEventHandler eventHandler;

    @Override
    public void asyncSend(Request request, Connection.Callback callback) {

        try {
            NetworkMessage<Request> networkMessage = new NetworkMessage<Request>();
            networkMessage.setVersion(Versions.currentVersion);
            networkMessage.setMethod(path.replace(".", "/") + "/transaction");
            networkMessage.newSeq();

            request.setResourceInfo(null);
            networkMessage.setData(request);

            NetworkCallback<Response> networkCallback =
                    new NetworkCallback<Response>() {
                        // Constructor
                        {
                            super.setTypeReference(
                                    new TypeReference<NetworkResponse<Response>>() {});
                        }

                        @Override
                        public void onResponse(
                                int status, String message, NetworkResponse<Response> msg) {
                            if (status != 0) {
                                Response response = new Response();
                                response.setErrorCode(StubQueryStatus.REMOTE_QUERY_FAILED);
                                response.setErrorMessage(
                                        "Async send remote connection status: "
                                                + status
                                                + ", message: "
                                                + message);
                                callback.onResponse(response);
                            } else {

                                callback.onResponse((Response) msg.getData());
                            }
                        }
                    };

            p2PService.asyncSendMessage(peer, networkMessage, networkCallback);

        } catch (Exception e) {

            Response response = new Response();
            response.setErrorCode(StubQueryStatus.REMOTE_QUERY_FAILED);
            response.setErrorMessage(
                    "Async send remote connection exception: " + e.getLocalizedMessage());
            callback.onResponse(response);
        }
    }

    @Override
    public void setConnectionEventHandler(ConnectionEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public Peer getPeer() {
        return peer;
    }

    public void setPeer(Peer peer) {
        this.peer = peer;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public P2PService getP2PService() {
        return p2PService;
    }

    public void setP2PService(P2PService p2PService) {
        this.p2PService = p2PService;
    }
}
