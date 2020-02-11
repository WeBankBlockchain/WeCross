package com.webank.wecross.stub.fabric;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.webank.wecross.common.ResourceQueryStatus;
import com.webank.wecross.common.WeCrossType;
import com.webank.wecross.resource.EventCallback;
import com.webank.wecross.resource.Path;
import com.webank.wecross.restserver.request.GetDataRequest;
import com.webank.wecross.restserver.request.SetDataRequest;
import com.webank.wecross.restserver.request.TransactionRequest;
import com.webank.wecross.restserver.response.GetDataResponse;
import com.webank.wecross.restserver.response.SetDataResponse;
import com.webank.wecross.restserver.response.TransactionResponse;
import com.webank.wecross.utils.core.HashUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.codec.binary.Hex;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.BlockEvent.TransactionEvent;
import org.hyperledger.fabric.sdk.BlockchainInfo;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.QueryByChaincodeRequest;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabricContractResource extends FabricResource {

    private Logger logger = LoggerFactory.getLogger(FabricContractResource.class);

    @JsonIgnore private String type;

    public void setType(String type) {
        this.type = type;
    }

    @JsonIgnore boolean hasInit = false;

    public FabricConn getFabricConn() {
        return fabricConn;
    }

    public void setFabricConn(FabricConn fabricConn) {
        this.fabricConn = fabricConn;
    }

    @JsonIgnore private FabricConn fabricConn = new FabricConn();

    private String checksum;

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public void init(FabricConn fabricConn) {
        if (!hasInit) {
            this.fabricConn = fabricConn;
            hasInit = true;
        }
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public GetDataResponse getData(GetDataRequest request) {
        return super.getData(request);
    }

    @Override
    public SetDataResponse setData(SetDataRequest request) {
        return super.setData(request);
    }

    private TransactionResponse checkRetTypes(String retTypes[]) {
        TransactionResponse transactionResponse = new FabricResponse();
        if (retTypes != null && retTypes.length != 0) {
            if (retTypes.length > 1
                    || !retTypes[0].trim().equals("String") && !retTypes[0].trim().equals("")) {
                transactionResponse.setErrorCode(ResourceQueryStatus.UNSUPPORTED_TYPE);
                transactionResponse.setErrorMessage(
                        "Unsupported return type for "
                                + WeCrossType.RESOURCE_TYPE_FABRIC_CONTRACT
                                + ": "
                                + Arrays.toString(retTypes));
            }
        }
        return transactionResponse;
    }

    public static String[] getParamterList(TransactionRequest request) {
        String[] paramterList = null;
        if (request.getArgs().length == 0) {
            paramterList = new String[] {};
        } else {
            paramterList = new String[request.getArgs().length];
            for (int i = 0; i < request.getArgs().length; i++) {
                paramterList[i] = String.valueOf(request.getArgs()[i]);
            }
        }

        return paramterList;
    }

    @Override
    public TransactionResponse call(TransactionRequest request) {

        TransactionResponse transactionResponse = checkRetTypes(request.getRetTypes());
        if (transactionResponse.getErrorCode() != 0) {
            return transactionResponse;
        }

        QueryByChaincodeRequest queryRequest = fabricConn.getHfClient().newQueryProposalRequest();
        queryRequest.setChaincodeID(fabricConn.getChaincodeID());
        queryRequest.setFcn(request.getMethod());

        String[] paramterList = getParamterList(request);
        queryRequest.setArgs(paramterList);
        try {
            ProposalResponse[] responses =
                    fabricConn
                            .getChannel()
                            .queryByChaincode(queryRequest)
                            .toArray(new ProposalResponse[0]);
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
        } catch (InvalidArgumentException | ProposalException e) {
            logger.error(
                    "query failed method:{} chainname:{}",
                    request.getMethod(),
                    fabricConn.getChainCodeName());
            transactionResponse.setErrorCode(ResourceQueryStatus.FABRIC_INVOKE_CHAINCODE_FAIL);
        }
        return transactionResponse;
    }

    @Override
    public String getChecksum() {
        try {
            if (checksum == null || checksum.equals("")) {
                checksum = HashUtils.sha256String(fabricConn.getChainCodeName());
            }
            return checksum;

        } catch (Exception e) {
            logger.error("Caculate checksum exception: " + e);
        }
        return null;
    }

    @Override
    public TransactionResponse sendTransaction(TransactionRequest request) {

        TransactionResponse transactionResponse = checkRetTypes(request.getRetTypes());
        if (transactionResponse.getErrorCode() != 0) {
            return transactionResponse;
        }

        TransactionProposalRequest transactionProposalRequest =
                fabricConn.getHfClient().newTransactionProposalRequest();
        transactionProposalRequest.setChaincodeID(fabricConn.getChaincodeID());
        transactionProposalRequest.setChaincodeLanguage(fabricConn.getChainCodeType());
        transactionProposalRequest.setFcn(request.getMethod());
        String[] paramterList = getParamterList(request);
        transactionProposalRequest.setArgs(paramterList);
        transactionProposalRequest.setProposalWaitTime(fabricConn.getProposalWaitTime());
        List<ProposalResponse> successful = new LinkedList<>();
        List<ProposalResponse> failed = new LinkedList<>();
        List<Object> resultList = new ArrayList<Object>();
        try {

            Collection<ProposalResponse> transactionPropResp =
                    fabricConn.getChannel().sendTransactionProposal(transactionProposalRequest);
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
                CompletableFuture<TransactionEvent> carfuture =
                        fabricConn.getChannel().sendTransaction(successful);
                long transactionTimeout = 5000000;
                try {
                    BlockEvent.TransactionEvent transactionEvent =
                            carfuture.get(transactionTimeout, TimeUnit.MILLISECONDS);
                    if (transactionEvent.isValid()) {
                        BlockchainInfo channelInfo = fabricConn.getChannel().queryBlockchainInfo();
                        transactionResponse.setHash(
                                Hex.encodeHexString(channelInfo.getCurrentBlockHash()));
                        transactionResponse.setErrorCode(0);

                        logger.info(
                                "Wait event success: "
                                        + transactionEvent.getChannelId()
                                        + " "
                                        + transactionEvent.getTransactionID()
                                        + " "
                                        + transactionEvent.getType()
                                        + " "
                                        + transactionEvent.getValidationCode());
                    } else {
                        transactionResponse.setErrorCode(
                                ResourceQueryStatus.FABRIC_COMMIT_CHAINCODE_FAIL);
                        logger.info(
                                "Wait event failed: "
                                        + transactionEvent.getChannelId()
                                        + " "
                                        + transactionEvent.getTransactionID()
                                        + " "
                                        + transactionEvent.getType()
                                        + " "
                                        + transactionEvent.getValidationCode());
                    }

                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    logger.error(
                            "query failed method:{} chainname:{}",
                            request.getMethod(),
                            fabricConn.getChainCodeName());
                    transactionResponse.setErrorCode(
                            ResourceQueryStatus.FABRIC_INVOKE_CHAINCODE_FAIL);
                    return transactionResponse;
                }
            } else {
                transactionResponse.setErrorCode(ResourceQueryStatus.FABRIC_INVOKE_CHAINCODE_FAIL);
                return transactionResponse;
            }
        } catch (ProposalException e) {
            logger.error(
                    "query failed method:{} chainname:{}",
                    request.getMethod(),
                    fabricConn.getChainCodeName());
            transactionResponse.setErrorCode(ResourceQueryStatus.FABRIC_INVOKE_CHAINCODE_FAIL);
        } catch (InvalidArgumentException e) {
            logger.error(
                    "query failed method:{} chainname:{}",
                    request.getMethod(),
                    fabricConn.getChainCodeName());
            transactionResponse.setErrorCode(ResourceQueryStatus.FABRIC_INVOKE_CHAINCODE_FAIL);
        }
        return transactionResponse;
    }

    @Override
    public void registerEventHandler(EventCallback callback) {
        super.registerEventHandler(callback);
    }

    @Override
    public TransactionRequest createRequest() {
        return super.createRequest();
    }

    @Override
    public int getDistance() {
        return super.getDistance();
    }

    @Override
    public Path getPath() {
        return super.getPath();
    }

    @Override
    public void setPath(Path path) {
        super.setPath(path);
    }

    @Override
    public String getPathAsString() {
        return super.getPathAsString();
    }
}
