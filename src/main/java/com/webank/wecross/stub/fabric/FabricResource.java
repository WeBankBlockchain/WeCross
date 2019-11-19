package com.webank.wecross.stub.fabric;

import com.webank.wecross.p2p.Peer;
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

    @Override
    public String getType() {
        return "FABRIC_CONTRACT";
    }

    @Override
    public GetDataResponse getData(GetDataRequest request) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SetDataResponse setData(SetDataRequest request) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TransactionResponse call(TransactionRequest request) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TransactionResponse sendTransaction(TransactionRequest request) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void registerEventHandler(EventCallback callback) {
        // TODO Auto-generated method stub

    }

    @Override
    public TransactionRequest createRequest() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getDistance() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Path getPath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setPath(Path path) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getPathAsString() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Peer> getPeers() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setPeers(Set<Peer> peers) {
        // TODO Auto-generated method stub

    }
}
