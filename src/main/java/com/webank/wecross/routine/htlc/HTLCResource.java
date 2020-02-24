package com.webank.wecross.routine.htlc;

import com.webank.wecross.common.ResourceQueryStatus;
import com.webank.wecross.common.WeCrossType;
import com.webank.wecross.exception.WeCrossException;
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
import java.util.Arrays;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTLCResource implements Resource {

    private Logger logger = LoggerFactory.getLogger(HTLCResource.class);

    private Resource originResource;

    public HTLCResource(Resource originResource) {
        this.originResource = originResource;
    }

    @Override
    public String getType() {
        return WeCrossType.RESOURCE_TYPE_HTLC_CONTRACT;
    }

    @Override
    public GetDataResponse getData(GetDataRequest request) {
        return originResource.getData(request);
    }

    @Override
    public SetDataResponse setData(SetDataRequest request) {
        return originResource.setData(request);
    }

    @Override
    public ProposalResponse callProposal(ProposalRequest request) {
        return originResource.callProposal(request);
    }

    @Override
    public ProposalResponse sendTransactionProposal(ProposalRequest request) {
        return originResource.sendTransactionProposal(request);
    }

    @Override
    public TransactionResponse call(TransactionRequest request) {
        return originResource.call(request);
    }

    @Override
    public TransactionResponse sendTransaction(TransactionRequest request) {
        TransactionRequest newRequest;
        try {
            newRequest = handleRequest(request);
        } catch (WeCrossException e) {
            TransactionResponse transactionResponse = new TransactionResponse();
            transactionResponse.setErrorCode(e.getErrorCode());
            transactionResponse.setErrorMessage(e.getMessage());
            return transactionResponse;
        }

        return originResource.sendTransaction(newRequest);
    }

    @Override
    public void registerEventHandler(EventCallback callback) {}

    @Override
    public TransactionRequest createRequest() {
        return originResource.createRequest();
    }

    @Override
    public int getDistance() {
        return 0;
    }

    @Override
    public String getChecksum() {
        return originResource.getChecksum();
    }

    @Override
    public String getContractAddress() {
        return originResource.getContractAddress();
    }

    @Override
    public Path getPath() {
        return originResource.getPath();
    }

    @Override
    public void setPath(Path path) {
        originResource.setPath(path);
    }

    @Override
    public String getPathAsString() {
        return originResource.getPathAsString();
    }

    @Override
    public Set<Peer> getPeers() {
        return originResource.getPeers();
    }

    @Override
    public void setPeers(Set<Peer> peers) {
        originResource.setPeers(peers);
    }

    @Override
    public String getCryptoSuite() {
        return null;
    }

    private TransactionRequest handleRequest(TransactionRequest request) throws WeCrossException {
        if (request.getMethod().equals("unlock")) {
            Object[] args = request.getArgs();
            if (args == null || args.length < 2) {
                logger.error("format of request is error in sendTransaction for unlock");
                throw new WeCrossException(
                        ResourceQueryStatus.HTLC_REQUEST_ERROR,
                        "hash of lock transaction not found");
            }
            String transactionHash = (String) args[0];

            // Verify that the asset is locked
            if (!HTLC.verifyLock(transactionHash)) {
                throw new WeCrossException(
                        ResourceQueryStatus.HTLC_VERIFY_LOCK_ERROR,
                        "verify transaction of lock failed");
            }

            Object[] newArgs = Arrays.copyOfRange(args, 1, args.length - 1);
            request.setArgs(newArgs);
        }

        logger.info("HTLCRequest: {}", request.toString());
        return request;
    }
}
