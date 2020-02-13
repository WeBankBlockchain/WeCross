package com.webank.wecross.stub.bcos;

import com.webank.wecross.peer.PeerInfo;
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
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;

public class BCOSResource implements Resource {

    protected Path path;

    public void init(Service service, Web3j web3j, Credentials credentials) {}

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
        return 0;
    }

    @Override
    public String getChecksum() {
        return null;
    }

    @Override
    public String getType() {
        return "BCOS_RESOURCE";
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
    public Set<PeerInfo> getPeers() {
        return null;
    }

    @Override
    public void setPeers(Set<PeerInfo> peers) {}
}
