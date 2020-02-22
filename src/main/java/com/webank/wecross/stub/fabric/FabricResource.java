package com.webank.wecross.stub.fabric;

import com.webank.wecross.common.WeCrossType;
import com.webank.wecross.peer.Peer;
import com.webank.wecross.resource.EventCallback;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.restserver.request.GetDataRequest;
import com.webank.wecross.restserver.request.SetDataRequest;
import com.webank.wecross.restserver.request.TransactionRequest;
import com.webank.wecross.restserver.response.GetDataResponse;
import com.webank.wecross.restserver.response.SetDataResponse;
import com.webank.wecross.restserver.response.TransactionResponse;
import java.util.Set;

public class FabricResource implements Resource {

    protected Path path;

    @Override
    public String getType() {
        return WeCrossType.RESOURCE_TYPE_FABRIC_CONTRACT;
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

    @Override
    public int getDistance() {
        return 0;
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
    public String getPathAsString() {
        return path.toString();
    }

    @Override
    public Set<Peer> getPeers() {
        return null;
    }

    @Override
    public void setPeers(Set<Peer> peers) {}

    @Override
    public String getChecksum() {
        return null;
    }
}
