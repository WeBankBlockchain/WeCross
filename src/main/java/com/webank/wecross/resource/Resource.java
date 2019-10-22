package com.webank.wecross.resource;

import com.webank.wecross.host.Peer;
import java.util.Set;

public abstract class Resource {
    protected Set<Peer> peers;
    protected Path path;

    public abstract GetDataResponse getData(GetDataRequest request);

    public abstract SetDataResponse setData(SetDataRequest request);

    public abstract TransactionResponse call(TransactionRequest request);

    public abstract TransactionResponse sendTransaction(TransactionRequest request);

    public abstract void registerEventHandler(EventCallback callback);

    public abstract TransactionRequest createRequest();

    public abstract int getDistance(); // 0 local, > 0 remote

    public Set<Peer> getPeers() {
        return peers;
    }

    public void setPeers(Set<Peer> peers) {
        this.peers = peers;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }
}
