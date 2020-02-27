package com.webank.wecross.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.webank.wecross.peer.Peer;
import com.webank.wecross.restserver.request.GetDataRequest;
import com.webank.wecross.restserver.request.ProposalRequest;
import com.webank.wecross.restserver.request.SetDataRequest;
import com.webank.wecross.restserver.request.TransactionRequest;
import com.webank.wecross.restserver.response.GetDataResponse;
import com.webank.wecross.restserver.response.ProposalResponse;
import com.webank.wecross.restserver.response.SetDataResponse;
import com.webank.wecross.restserver.response.TransactionResponse;
import java.util.Set;

public interface Resource {

    String getType();

    GetDataResponse getData(GetDataRequest request);

    SetDataResponse setData(SetDataRequest request);

    ProposalResponse callProposal(ProposalRequest request);

    ProposalResponse sendTransactionProposal(ProposalRequest request);

    TransactionResponse call(TransactionRequest request);

    TransactionResponse sendTransaction(TransactionRequest request);

    void registerEventHandler(EventCallback callback);

    TransactionRequest createRequest();

    int getDistance(); // 0 local, > 0 remote

    String getChecksum();

    @JsonIgnore
    String getContractAddress();

    @JsonIgnore
    Path getPath();

    void setPath(Path path);

    @JsonProperty("path")
    String getPathAsString();

    @JsonIgnore
    Set<Peer> getPeers();

    void setPeers(Set<Peer> peers);
}
