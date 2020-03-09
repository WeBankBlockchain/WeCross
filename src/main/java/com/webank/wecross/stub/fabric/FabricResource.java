package com.webank.wecross.stub.fabric;

import com.webank.wecross.peer.Peer;
import com.webank.wecross.resource.EventCallback;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.restserver.request.GetDataRequest;
import com.webank.wecross.restserver.request.ProposalRequest;
import com.webank.wecross.restserver.request.SetDataRequest;
import com.webank.wecross.restserver.request.TransactionRequest;
import com.webank.wecross.restserver.response.GetDataResponse;
import com.webank.wecross.restserver.response.ProposalResponse;
import com.webank.wecross.restserver.response.SetDataResponse;
import com.webank.wecross.restserver.response.TransactionResponse;
import java.util.Set;

public class FabricResource extends Resource {

    protected Path path;

    @Override
    public String getType() {
        return "FABRIC_RESOURCES";
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
    public int getDistance() {
        return 0;
    }

    @Override
    public String getChecksum() {
        return null;
    }
}
