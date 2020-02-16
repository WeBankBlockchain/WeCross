package com.webank.wecross.resource;

import com.webank.wecross.common.WeCrossType;
import com.webank.wecross.p2p.netty.common.Peer;
import com.webank.wecross.restserver.request.GetDataRequest;
import com.webank.wecross.restserver.request.ProposalRequest;
import com.webank.wecross.restserver.request.SetDataRequest;
import com.webank.wecross.restserver.request.TransactionRequest;
import com.webank.wecross.restserver.response.GetDataResponse;
import com.webank.wecross.restserver.response.ProposalResponse;
import com.webank.wecross.restserver.response.SetDataResponse;
import com.webank.wecross.restserver.response.TransactionResponse;
import com.webank.wecross.utils.core.HashUtils;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// No reliable chain, just respond what you call
public class TestResource implements Resource {
    private Logger logger = LoggerFactory.getLogger(TestResource.class);

    protected Path path;
    protected String checksum;

    @Override
    public String getType() {
        return WeCrossType.RESOURCE_TYPE_TEST;
    }

    @Override
    public GetDataResponse getData(GetDataRequest request) {
        GetDataResponse response = new GetDataResponse();
        response.setErrorCode(0);
        response.setErrorMessage("getData test resource success");
        response.setValue(request.toString());
        return response;
    }

    @Override
    public SetDataResponse setData(SetDataRequest request) {

        SetDataResponse response = new SetDataResponse();
        response.setErrorCode(0);
        response.setErrorMessage("setData test resource success");
        return response;
    }

    @Override
    public ProposalResponse callProposal(ProposalRequest request) {
        return null;
    }

    @Override
    public ProposalResponse sendTransactionProposal(ProposalRequest request) {
        return null;
    }

    @Override
    public TransactionResponse call(TransactionRequest request) {
        TransactionResponse response = new TransactionResponse();
        response.setErrorCode(0);
        response.setErrorMessage("call test resource success");
        response.setHash("010157f4");
        response.setResult(new Object[] {request});
        return response;
    }

    @Override
    public TransactionResponse sendTransaction(TransactionRequest request) {
        TransactionResponse response = new TransactionResponse();
        response.setErrorCode(0);
        response.setErrorMessage("sendTransaction test resource success");
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
    public String getChecksum() {
        try {
            if (checksum == null || checksum.equals("")) {
                checksum = HashUtils.sha256String(path.toString());
            }
            return checksum;

        } catch (Exception e) {
            logger.error("Caculate checksum exception: " + e);
        }
        return null;
    }

    @Override
    public String getContractAddress() {
        return null;
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
