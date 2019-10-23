package com.webank.wecross.resource;

import com.webank.wecross.host.Peer;
import com.webank.wecross.resource.request.GetDataRequest;
import com.webank.wecross.resource.request.SetDataRequest;
import com.webank.wecross.resource.request.TransactionRequest;
import com.webank.wecross.resource.response.GetDataResponse;
import com.webank.wecross.resource.response.SetDataResponse;
import com.webank.wecross.resource.response.TransactionResponse;
import java.util.Set;

// No reliable chain, just respond what you call
public class SimpleResource implements Resource {

    protected Path path;

    @Override
    public String getType() {
        return "SIMPLE_RESOURCE";
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
        response.setErrorCode(0);
        response.setErrorMessage("Call simplie resource success");
        response.setHash("010157f4");
        response.setResult(new Object[] {request});
        return response;
    }

    @Override
    public TransactionResponse sendTransaction(TransactionRequest request) {
        TransactionResponse response = new TransactionResponse();
        response.setErrorCode(0);
        response.setErrorMessage("sendTransaction simplie resource success");
        response.setHash("010157f4");
        response.setResult(new Object[] {request});
        return response;
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

    @Override
    public Set<Peer> getPeers() {
        return null;
    }

    @Override
    public void setPeers(Set<Peer> peers) {}
}
