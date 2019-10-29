package com.webank.wecross.stub.remote;

import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.P2PMessageCallback;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.Peer;
import com.webank.wecross.p2p.engine.restful.P2PHttpResponse;
import com.webank.wecross.resource.EventCallback;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.restserver.request.GetDataRequest;
import com.webank.wecross.restserver.request.SetDataRequest;
import com.webank.wecross.restserver.request.TransactionRequest;
import com.webank.wecross.restserver.response.GetDataResponse;
import com.webank.wecross.restserver.response.SetDataResponse;
import com.webank.wecross.restserver.response.TransactionResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;

public class RemoteResource implements Resource {

    private Logger logger = LoggerFactory.getLogger(RemoteResource.class);
    private P2PMessageEngine p2pEngine;
    private int distance; // How many jumps to local stub
    private Set<Peer> peers;
    private Path path;

    public RemoteResource(Set<Peer> peers, int distance, P2PMessageEngine p2pEngine) {
        setPeers(peers);
        this.distance = distance;
        this.p2pEngine = p2pEngine;
    }

    public RemoteResource(Peer peer, int distance, P2PMessageEngine p2pEngine) {
        Set<Peer> peers = new HashSet<>();
        peers.add(peer);
        setPeers(peers);
        this.distance = distance;
        this.p2pEngine = p2pEngine;
    }

    public Set<Peer> getPeers() {
        return peers;
    }

    public void setPeers(Set<Peer> peers) {
        this.peers = peers;
    }

    @Override
    public String getType() {
        return "REMOTE_RESOURCE";
    }

    @Override
    public GetDataResponse getData(GetDataRequest request) {

        return null;
    }

    @Override
    public SetDataResponse setData(SetDataRequest request) {

        return null;
    }

    @Override
    public TransactionResponse call(TransactionRequest request) {
        TransactionResponse response = new TransactionResponse();
        try {
            List<Peer> peerList = getRandPeerList();
            for (Peer peerToSend : peerList) {
                try {
                    P2PMessage<TransactionRequest> p2pReq = new P2PMessage<>();
                    p2pReq.setVersion("0.1");
                    p2pReq.newSeq();
                    p2pReq.setMethod(path.toURI() + "/call");
                    p2pReq.setData(request);

                    response = (TransactionResponse) sendRemote(peerToSend, p2pReq);

                    if (response.getErrorCode() == 0) {
                        return response;
                    }
                } catch (Exception e) {
                    continue;
                }
            }
            throw new Exception("Not an available peer to request");
        } catch (Exception e) {
            response.setErrorCode(-1);
            response.setErrorMessage("Call remote resource exception");
        }
        return response;
    }

    @Override
    public TransactionResponse sendTransaction(TransactionRequest request) {

        TransactionResponse response = new TransactionResponse();
        try {
            List<Peer> peerList = getRandPeerList();
            for (Peer peerToSend : peerList) {
                try {
                    P2PMessage<TransactionRequest> p2pReq = new P2PMessage<>();
                    p2pReq.setVersion("0.1");
                    p2pReq.newSeq();
                    p2pReq.setMethod(path.toURI() + "/sendTransaction");
                    p2pReq.setData(request);

                    response = (TransactionResponse) sendRemote(peerToSend, p2pReq);

                    if (response.getErrorCode() == 0) {
                        return response;
                    }
                } catch (Exception e) {
                    continue;
                }
            }
            throw new Exception("Not an available peer to request");
        } catch (Exception e) {
            response.setErrorCode(-1);
            response.setErrorMessage("Call remote resource exception");
        }
        return response;
    }

    @Override
    public void registerEventHandler(EventCallback callback) {

        return;
    }

    @Override
    public TransactionRequest createRequest() {

        return null;
    }

    @Override
    public int getDistance() {
        return distance;
    }

    @Override
    public Path getPath() {
        return this.path;
    }

    @Override
    public void setPath(Path path) {
        this.path = path;
    }

    @Override
    public String getPathAsString() {
        return path.toString();
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    private List<Peer> getRandPeerList() throws Exception {
        if (peers == null || peers.isEmpty()) {
            throw new Exception("Peers of the resource is empty");
        }

        List<Peer> peerList = new ArrayList<>(peers);
        Collections.shuffle(peerList);
        return peerList;
    }

    private TransactionResponse sendRemote(Peer peer, P2PMessage request) throws Exception {

        class SemaphoreCallback extends P2PMessageCallback {

            public transient Semaphore semaphore = new Semaphore(1, true);
            private TransactionResponse responseData;

            public SemaphoreCallback() {
                super.setEngineCallbackMessageClassType(
                        new ParameterizedTypeReference<P2PHttpResponse<TransactionResponse>>() {});
                try {
                    semaphore.acquire(1);

                } catch (InterruptedException e) {
                    logger.error("error:", e);
                }
            }

            @Override
            public void onResponse(int status, String message, P2PMessage msg) {
                responseData = (TransactionResponse) msg.getData();
                semaphore.release();
            }

            public TransactionResponse getResponseData() {
                return responseData;
            }
        }

        SemaphoreCallback callback = new SemaphoreCallback();

        callback.setPeer(peer);

        logger.info(
                "Request remote resource: method:{}, data:{}",
                request.getMethod(),
                request.getData().toString());
        p2pEngine.asyncSendMessage(peer, request, callback);

        callback.semaphore.acquire(1);

        logger.info(
                "Respond from remote resource: status:{}, data:{}",
                callback.getStatus(),
                callback.getResponseData().toString());
        if (callback.getStatus() != 0) {
            throw new Exception(callback.getMessage());
        }

        return callback.getResponseData();
    }
}
