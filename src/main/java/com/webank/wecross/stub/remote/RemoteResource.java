package com.webank.wecross.stub.remote;

import com.webank.wecross.host.Peer;
import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.P2PMessageEngineFactory;
import com.webank.wecross.p2p.P2PMessageSemaphoreCallback;
import com.webank.wecross.resource.EventCallback;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.resource.request.GetDataRequest;
import com.webank.wecross.resource.request.SetDataRequest;
import com.webank.wecross.resource.request.TransactionRequest;
import com.webank.wecross.resource.response.GetDataResponse;
import com.webank.wecross.resource.response.SetDataResponse;
import com.webank.wecross.resource.response.TransactionResponse;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteResource implements Resource {

    private Logger logger = LoggerFactory.getLogger(RemoteResource.class);
    private P2PMessageEngine p2pEngine = P2PMessageEngineFactory.getEngineInstance();
    private int distance; // How many jumps to local stub
    private Set<Peer> peers;

    private Path path;

    public Set<Peer> getPeers() {
        return peers;
    }

    public void setPeers(Set<Peer> peers) {
        this.peers = peers;
    }

    public RemoteResource(Set<Peer> peers, int distance) {
        setPeers(peers);
        this.distance = distance;
    }

    public RemoteResource(Peer peer, int distance) {
        Set<Peer> peers = new HashSet<>();
        peers.add(peer);
        setPeers(peers);
        this.distance = distance;
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
        return callOrSendTransactionRemote("call", request);
    }

    @Override
    public TransactionResponse sendTransaction(TransactionRequest request) {
        return callOrSendTransactionRemote("sendTransaction", request);
    }

    @Override
    public void registerEventHandler(EventCallback callback) {}

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

    private Peer getPrimaryPeerToSend() throws Exception {
        if (peers == null || peers.isEmpty()) {
            throw new Exception("Peers of the resource is empty");
        }
        Peer firstPeer = this.peers.stream().findFirst().get();
        return firstPeer;
    }

    private Object sendRemote(Peer peer, P2PMessage request, P2PMessageSemaphoreCallback callback)
            throws Exception {

        p2pEngine.asyncSendMessage(peer, request, callback);

        callback.semaphore.acquire(1);

        if (callback.getStatus() != 0) {
            throw new Exception(callback.getMessage());
        }

        return callback.getResponseData();
    }

    private TransactionResponse callOrSendTransactionRemote(
            String method, TransactionRequest request) {
        TransactionResponse response = new TransactionResponse();
        try {
            Peer peerToSend = getPrimaryPeerToSend();

            TransactionRequestMessageData data = new TransactionRequestMessageData();
            data.setVersion("0.1");
            data.setPath(getPath().toString());
            data.setMethod(method);
            data.setData(request);

            P2PMessage<TransactionRequestMessageData> p2pReq = new P2PMessage<>();
            p2pReq.setVersion("0.1");
            p2pReq.newSeq();
            p2pReq.setType("remote");
            p2pReq.setData(data);

            TransactionResponseCallback callback = new TransactionResponseCallback();
            callback.setPeer(peerToSend);

            response = (TransactionResponse) sendRemote(peerToSend, p2pReq, callback);
        } catch (Exception e) {
            response.setErrorCode(-1);
            response.setErrorMessage("Call remote resource error, exception:" + e.getMessage());
        }
        return response;
    }
}
