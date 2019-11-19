package com.webank.wecross.stub.fabric;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.ByteString;
import com.webank.wecross.p2p.Peer;
import com.webank.wecross.resource.EventCallback;
import com.webank.wecross.resource.Path;
import com.webank.wecross.restserver.request.GetDataRequest;
import com.webank.wecross.restserver.request.SetDataRequest;
import com.webank.wecross.restserver.request.TransactionRequest;
import com.webank.wecross.restserver.response.GetDataResponse;
import com.webank.wecross.restserver.response.SetDataResponse;
import com.webank.wecross.restserver.response.TransactionResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.codec.binary.Hex;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.BlockEvent.TransactionEvent;
import org.hyperledger.fabric.sdk.BlockchainInfo;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.QueryByChaincodeRequest;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class FabricContractResource extends FabricResource {

    private Logger logger = LoggerFactory.getLogger(FabricContractResource.class);

    @JsonIgnore boolean hasInit = false;

    @JsonIgnore private HFClient client;
    @JsonIgnore private Channel channel;
    @JsonIgnore ChaincodeID chaincodeID;
    @JsonIgnore String chainName;

    public String getChainName() {
        return chainName;
    }

    public void setChainName(String chainName) {
        this.chainName = chainName;
    }

    void init(HFClient client, Channel channel) {
        if (!hasInit) {
            this.client = client;
            this.channel = channel;
            chaincodeID = ChaincodeID.newBuilder().setName(chainName).build();
            hasInit = true;
        }
    }

    @Override
    public String getType() {
        // TODO Auto-generated method stub
        return super.getType();
    }

    @Override
    public GetDataResponse getData(GetDataRequest request) {
        // TODO Auto-generated method stub
        return super.getData(request);
    }

    @Override
    public SetDataResponse setData(SetDataRequest request) {
        // TODO Auto-generated method stub
        return super.setData(request);
    }

    @Override
    public TransactionResponse call(TransactionRequest request) {
        TransactionResponse transactionResponse = new FabricResponse();
        QueryByChaincodeRequest queryRequest = client.newQueryProposalRequest();
        queryRequest.setChaincodeID(chaincodeID);
        queryRequest.setFcn(request.getMethod());

        String[] paramterList = null;
        if (request.getArgs().length == 0) {
            paramterList = new String[] {};
        } else {
            paramterList = new String[request.getArgs().length];
            for (int i = 0; i < request.getArgs().length; i++) {
                paramterList[i] = String.valueOf(request.getArgs()[i]);
            }
        }
        queryRequest.setArgs(paramterList);
        try {
            ProposalResponse[] responses =
                    channel.queryByChaincode(queryRequest).toArray(new ProposalResponse[0]);
            List<Object> resultList = new ArrayList<Object>();
            if (responses.length > 0) {
                resultList.add(
                        responses[0]
                                .getProposalResponse()
                                .getResponse()
                                .getPayload()
                                .toStringUtf8());
            }
            transactionResponse.setErrorCode(0);
            transactionResponse.setErrorMessage("");
            transactionResponse.setResult(resultList.toArray());
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        } catch (ProposalException e) {
            e.printStackTrace();
        }
        return transactionResponse;
    }
    
    @Override
    public TransactionResponse sendTransaction(TransactionRequest request) {
        TransactionResponse transactionResponse = new FabricResponse();
        TransactionProposalRequest transactionProposalRequest =
                client.newTransactionProposalRequest();
        transactionProposalRequest.setChaincodeID(chaincodeID);
        transactionProposalRequest.setChaincodeLanguage(
                org.hyperledger.fabric.sdk.TransactionRequest.Type.GO_LANG);
        transactionProposalRequest.setFcn(request.getMethod());
        String[] paramterList = null;
        if (request.getArgs().length == 0) {
            paramterList = new String[] {};
        } else {
            paramterList = new String[request.getArgs().length];
            for (int i = 0; i < request.getArgs().length; i++) {
                paramterList[i] = String.valueOf(request.getArgs()[i]);
            }
        }
        transactionProposalRequest.setArgs(paramterList);
        transactionProposalRequest.setProposalWaitTime(120000);
        List<ProposalResponse> successful = new LinkedList<>();
        List<ProposalResponse> failed = new LinkedList<>();
        List<Object> resultList = new ArrayList<Object>();
        try {

            Collection<ProposalResponse> transactionPropResp =
                    channel.sendTransactionProposal(transactionProposalRequest);
            Boolean result = true;
            for (ProposalResponse response : transactionPropResp) {
                if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                    logger.info(
                            "[√] Got success response from peer:{} , payload:{}",
                            response.getPeer().getName(),
                            new String(response.getChaincodeActionResponsePayload()));
                    successful.add(response);
                    resultList.add(new String(response.getChaincodeActionResponsePayload()));
                } else {
                    result = false;
                    String status = response.getStatus().toString();
                    logger.error(
                            "[×] Got failed response from peer:{}, status:{}, error message:{}",
                            response.getPeer().getName(),
                            status,
                            response.getMessage());
                    failed.add(response);
                }
            }
            transactionResponse.setResult(resultList.toArray());
            if (result) {
                logger.info("Sending transaction to orderers...");
                CompletableFuture<TransactionEvent> carfuture = channel.sendTransaction(successful);
                long transactionTimeout = 5000000;
                try {
                    BlockEvent.TransactionEvent transactionEvent =
                            carfuture.get(transactionTimeout, TimeUnit.MILLISECONDS);
                    BlockchainInfo channelInfo = channel.queryBlockchainInfo();
                    transactionResponse.setHash(Hex.encodeHexString(channelInfo.getCurrentBlockHash()));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (TimeoutException e) {
                    e.printStackTrace();
                }
            }
        } catch (ProposalException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
        return transactionResponse;
    }

    @Override
    public void registerEventHandler(EventCallback callback) {
        // TODO Auto-generated method stub
        super.registerEventHandler(callback);
    }

    @Override
    public TransactionRequest createRequest() {
        // TODO Auto-generated method stub
        return super.createRequest();
    }

    @Override
    public int getDistance() {
        // TODO Auto-generated method stub
        return super.getDistance();
    }

    @Override
    public Path getPath() {
        // TODO Auto-generated method stub
        return super.getPath();
    }

    @Override
    public void setPath(Path path) {
        // TODO Auto-generated method stub
        super.setPath(path);
    }

    @Override
    public String getPathAsString() {
        // TODO Auto-generated method stub
        return super.getPathAsString();
    }

    @Override
    public Set<Peer> getPeers() {
        // TODO Auto-generated method stub
        return super.getPeers();
    }

    @Override
    public void setPeers(Set<Peer> peers) {
        // TODO Auto-generated method stub
        super.setPeers(peers);
    }

    public HFClient getClient() {
        return client;
    }

    public void setClient(HFClient client) {
        this.client = client;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public ChaincodeID getChaincodeID() {
        return chaincodeID;
    }

    public void setChaincodeID(ChaincodeID chaincodeID) {
        this.chaincodeID = chaincodeID;
    }
}
