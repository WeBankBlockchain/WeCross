package com.webank.wecross.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.webank.wecross.p2p.Peer;
import com.webank.wecross.resource.request.GetDataRequest;
import com.webank.wecross.resource.request.SetDataRequest;
import com.webank.wecross.resource.request.TransactionRequest;
import com.webank.wecross.resource.response.GetDataResponse;
import com.webank.wecross.resource.response.SetDataResponse;
import com.webank.wecross.resource.response.TransactionResponse;
import java.util.Set;

public interface Resource {

    String getType();

    GetDataResponse getData(GetDataRequest request);

    SetDataResponse setData(SetDataRequest request);

    TransactionResponse call(TransactionRequest request);

    TransactionResponse sendTransaction(TransactionRequest request);

    void registerEventHandler(EventCallback callback);

    TransactionRequest createRequest();

    int getDistance(); // 0 local, > 0 remote

    @JsonIgnore
    Path getPath();

    void setPath(Path path);

    @JsonProperty("path")
    String getPathAsString();

    @JsonIgnore
    public Set<Peer> getPeers();

    public void setPeers(Set<Peer> peers);
}
