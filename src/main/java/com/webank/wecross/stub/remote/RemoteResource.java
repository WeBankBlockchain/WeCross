package com.webank.wecross.stub.remote;

import com.webank.wecross.host.Peer;
import com.webank.wecross.resource.*;

public class RemoteResource implements Resource {
    private int accessDepth; // How many jumps to local stub
    private Peer peer;
    private Path path;

    public RemoteResource(Peer peer, int accessDepth) {
        this.peer = peer;
        this.accessDepth = accessDepth;
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public void setPath(Path path) {
        this.path = path;
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
        return null;
    }

    @Override
    public TransactionResponse sendTransaction(TransactionRequest request) {
        return null;
    }

    @Override
    public void registerEventHandler(EventCallback callback) {}

    @Override
    public TransactionRequest createRequest() {
        return null;
    }

    public int getAccessDepth() {
        return accessDepth;
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    public void setAccessDepth(int accessDepth) {
        this.accessDepth = accessDepth;
    }

    public Peer getPeer() {
        return peer;
    }

    public void setPeer(Peer peer) {
        this.peer = peer;
    }
}
