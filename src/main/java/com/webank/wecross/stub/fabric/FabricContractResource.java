package com.webank.wecross.stub.fabric;

import static java.lang.String.format;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.ByteString;
import com.webank.wecross.common.ResourceQueryStatus;
import com.webank.wecross.common.WeCrossType;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.proposal.Proposal;
import com.webank.wecross.resource.EventCallback;
import com.webank.wecross.resource.Path;
import com.webank.wecross.restserver.request.GetDataRequest;
import com.webank.wecross.restserver.request.ProposalRequest;
import com.webank.wecross.restserver.request.SetDataRequest;
import com.webank.wecross.restserver.request.TransactionRequest;
import com.webank.wecross.restserver.response.GetDataResponse;
import com.webank.wecross.restserver.response.SetDataResponse;
import com.webank.wecross.restserver.response.TransactionResponse;
import com.webank.wecross.utils.core.HashUtils;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.codec.binary.Hex;
import org.hyperledger.fabric.protos.common.Common;
import org.hyperledger.fabric.protos.orderer.Ab;
import org.hyperledger.fabric.protos.peer.FabricProposalResponse;
import org.hyperledger.fabric.protos.peer.FabricTransaction;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.BlockEvent.TransactionEvent;
import org.hyperledger.fabric.sdk.BlockchainInfo;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.EventHub;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.QueryByChaincodeRequest;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.transaction.TransactionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabricContractResource extends FabricResource {

    private Logger logger = LoggerFactory.getLogger(FabricContractResource.class);

    private String type;
    private FabricProposalFactory proposalFactory;

    // Fabric inner functions
    private Method methodSendProposalToPeers;
    private Method methodSendTransactionToOrderer;
    private Method methodRegisterTxListener;

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

    @Override
    public String getType() {
        return type;
    }

    public void init(FabricConn fabricConn) {
        if (!hasInit) {
            this.fabricConn = fabricConn;
            proposalFactory = new FabricProposalFactory(fabricConn);
            hasInit = true;

            enableFabricInnerFunctions(fabricConn);
        }
    }

    private void enableFabricInnerFunctions(FabricConn fabricConn) {
        try {
            // enable channel.sendProposalToPeers()
            methodSendProposalToPeers =
                    fabricConn
                            .getChannel()
                            .getClass()
                            .getDeclaredMethod(
                                    "sendProposalToPeers",
                                    new Class[] {
                                        Collection.class,
                                        org.hyperledger.fabric.protos.peer.FabricProposal
                                                .SignedProposal.class,
                                        TransactionContext.class
                                    });
            methodSendProposalToPeers.setAccessible(true);

            // sendTransaction(Common.Envelope transaction)
            methodSendTransactionToOrderer =
                    Orderer.class.getDeclaredMethod(
                            "sendTransaction", new Class[] {Common.Envelope.class});
            methodSendTransactionToOrderer.setAccessible(true);

            // Channel.CompletableFuture<TransactionEvent> registerTxListener(String txid, NOfEvents
            // nOfEvents, boolean failFast)
            methodRegisterTxListener =
                    fabricConn
                            .getChannel()
                            .getClass()
                            .getDeclaredMethod(
                                    "registerTxListener",
                                    new Class[] {
                                        String.class, Channel.NOfEvents.class, boolean.class
                                    });
            methodRegisterTxListener.setAccessible(true);
        } catch (Exception e) {
            logger.error("enableFabricInnerFunctions exception: " + e.getMessage());
        }
    }

    /*
    @Override
    public com.webank.wecross.restserver.response.ProposalResponse callProposal(
            ProposalRequest request) {
        com.webank.wecross.restserver.response.ProposalResponse response =
                new com.webank.wecross.restserver.response.ProposalResponse();
        response.setSeq(request.getSeq());
        response.setCryptoSuite(getCryptoSuite());
        try {
            byte[] bytesToSign = generateEndorserProposalSignBytes(request);

            response.setErrorCode(0);
            response.setProposalToSign(bytesToSign);
            response.setType(WeCrossType.PROPOSAL_TYPE_PEER_PAYLODAD);
        } catch (Exception e) {
            response.setErrorCode(-1);
            response.setProposalToSign(new byte[] {});
            response.setErrorMessage("Call proposal error: " + e.getMessage());
        }

        return response;
    }

    @Override
    public com.webank.wecross.restserver.response.ProposalResponse sendTransactionProposal(
            ProposalRequest request) {

        try {
            if (request.getExtraData() == null) {
                return generateEndorserProposal(request);
            } else {
                return generateOrdererProposal(request);
            }

        } catch (Exception e) {
            com.webank.wecross.restserver.response.ProposalResponse response =
                    new com.webank.wecross.restserver.response.ProposalResponse();
            response.setSeq(request.getSeq());
            response.setCryptoSuite(getCryptoSuite());
            response.setErrorCode(-1);
            response.setProposalToSign(new byte[] {});
            response.setErrorMessage("Call proposal error: " + e.getMessage());
            return response;
        }
    }
    */

    @Override
    public TransactionResponse call(TransactionRequest request) {

        TransactionResponse transactionResponse = new FabricResponse();
        try {
            if (request.getSig() == null || request.getSig().length == 0) {
                transactionResponse = callWithRouterSign(request);
            } else {
                transactionResponse = callWithUserSign(request);
            }
        } catch (Exception e) {
            String errorMsg =
                    "query failed method: "
                            + request.getMethod()
                            + " chainname: "
                            + fabricConn.getChainCodeName()
                            + " exception: "
                            + e.getMessage();
            logger.error(errorMsg);
            transactionResponse.setErrorCode(ResourceQueryStatus.FABRIC_INVOKE_CHAINCODE_FAIL);
            transactionResponse.setErrorMessage(errorMsg);
        }
        return transactionResponse;
    }

    @Override
    public TransactionResponse sendTransaction(TransactionRequest request) {

        TransactionResponse transactionResponse = new FabricResponse();
        try {
            if (request.getSig() == null || request.getSig().length == 0) {
                transactionResponse = sendTransactionWithRouterSign(request);
            } else {
                transactionResponse = sendTransactionWithUserSign(request);
            }

        } catch (Exception e) {
            String errorMsg =
                    "query failed method: "
                            + request.getMethod()
                            + " chainname: "
                            + fabricConn.getChainCodeName()
                            + " exception: "
                            + e.getMessage();
            logger.error(errorMsg);
            transactionResponse.setErrorCode(ResourceQueryStatus.FABRIC_INVOKE_CHAINCODE_FAIL);
            transactionResponse.setErrorMessage(errorMsg);
        }
        return transactionResponse;
    }

    @Override
    public void registerEventHandler(EventCallback callback) {
        super.registerEventHandler(callback);
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

    //@Override
    public String getCryptoSuite() {
        return WeCrossType.CRYPTO_SUITE_FABRIC_BC_SECP256R1;
    }

    private TransactionResponse buildTransactionResponse(String retTypes[]) {
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

    public static String[] getParamterList(Object[] args) {
        String[] paramterList = null;
        if (args.length == 0) {
            paramterList = new String[] {};
        } else {
            paramterList = new String[args.length];
            for (int i = 0; i < args.length; i++) {
                paramterList[i] = String.valueOf(args[i]);
            }
        }

        return paramterList;
    }

    public static String[] getParamterList(TransactionRequest request) {
        return getParamterList(request.getArgs());
    }

    public static String[] getParamterList(ProposalRequest request) {
        return getParamterList(request.getArgs());
    }

    private TransactionResponse callWithUserSign(TransactionRequest request) throws Exception {
        TransactionResponse transactionResponse = buildTransactionResponse(request.getRetTypes());
        if (transactionResponse.getErrorCode() != 0) {
            return transactionResponse;
        }

        byte[] proposalBytes = request.getProposalBytes();
        FabricProposal proposal = proposalFactory.buildFromBytes(proposalBytes);

        if (!proposal.isEqualsRequest(request)) {
            throw new WeCrossException(
                    ResourceQueryStatus.INVALID_PROPOSAL_BYTES,
                    "Proposal bytes is not belongs to this request");
        }

        Collection<ProposalResponse> proposalResponses =
                sendFabricProposalRequest(proposal, request.getSig());

        List<ProposalResponse> successful = new LinkedList<>();
        List<ProposalResponse> failed = new LinkedList<>();

        for (ProposalResponse response : proposalResponses) {
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                logger.info(
                        "[√] Got success response from peer:{} , payload:{}",
                        response.getPeer().getName(),
                        new String(response.getChaincodeActionResponsePayload()));
                successful.add(response);
            } else {
                String status = response.getStatus().toString();
                logger.error(
                        "[×] Got failed response from peer:{}, status:{}, error message:{}",
                        response.getPeer().getName(),
                        status,
                        response.getMessage());
                failed.add(response);
            }
        }

        if (successful.size() > 0) {
            List<Object> resultList = new ArrayList<Object>();
            resultList.add(
                    successful
                            .get(0)
                            .getProposalResponse()
                            .getResponse()
                            .getPayload()
                            .toStringUtf8());

            transactionResponse.setErrorCode(0);
            transactionResponse.setErrorMessage("");
            transactionResponse.setResult(resultList.toArray());
        } else {
            transactionResponse.setErrorCode(ResourceQueryStatus.FABRIC_INVOKE_CHAINCODE_FAIL);
            transactionResponse.setErrorMessage(failed.get(0).getMessage());
        }

        return transactionResponse;
    }

    private TransactionResponse callWithRouterSign(TransactionRequest request) throws Exception {
        TransactionResponse transactionResponse = buildTransactionResponse(request.getRetTypes());
        if (transactionResponse.getErrorCode() != 0) {
            return transactionResponse;
        }

        QueryByChaincodeRequest queryRequest = fabricConn.getHfClient().newQueryProposalRequest();
        queryRequest.setChaincodeID(fabricConn.getChaincodeID());
        queryRequest.setFcn(request.getMethod());

        String[] paramterList = getParamterList(request);
        queryRequest.setArgs(paramterList);

        ProposalResponse[] responses =
                fabricConn
                        .getChannel()
                        .queryByChaincode(queryRequest)
                        .toArray(new ProposalResponse[0]);
        List<Object> resultList = new ArrayList<Object>();
        if (responses.length > 0) {
            resultList.add(
                    responses[0].getProposalResponse().getResponse().getPayload().toStringUtf8());
        }
        transactionResponse.setErrorCode(0);
        transactionResponse.setErrorMessage("");
        transactionResponse.setResult(resultList.toArray());
        return transactionResponse;
    }

    private TransactionResponse sendTransactionWithRouterSign(TransactionRequest request)
            throws Exception {
        TransactionResponse transactionResponse = buildTransactionResponse(request.getRetTypes());
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
            long transactionTimeout = 5000;
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
                transactionResponse.setErrorCode(ResourceQueryStatus.FABRIC_INVOKE_CHAINCODE_FAIL);
                return transactionResponse;
            }
        } else {
            transactionResponse.setErrorCode(ResourceQueryStatus.FABRIC_INVOKE_CHAINCODE_FAIL);
            return transactionResponse;
        }
        return transactionResponse;
    }

    private TransactionResponse sendTransactionWithUserSign(TransactionRequest request)
            throws Exception {

        logger.info("Sending transaction to orderers...");
        TransactionResponse transactionResponse = new TransactionResponse();
        try {
            transactionResponse = sendFabricOrdererRequest(request);

        } catch (Exception e) {
            logger.error(
                    "query failed method:{} chainname:{}",
                    request.getMethod(),
                    fabricConn.getChainCodeName());
            transactionResponse.setErrorCode(ResourceQueryStatus.FABRIC_INVOKE_CHAINCODE_FAIL);
            return transactionResponse;
        }

        return transactionResponse;
    }

    private com.webank.wecross.restserver.response.ProposalResponse generateEndorserProposal(
            ProposalRequest request) throws Exception {
        com.webank.wecross.restserver.response.ProposalResponse response =
                new com.webank.wecross.restserver.response.ProposalResponse();
        response.setSeq(request.getSeq());
        response.setCryptoSuite(getCryptoSuite());

        byte[] bytesToSign = generateEndorserProposalSignBytes(request);
        response.setErrorCode(0);
        response.setProposalToSign(bytesToSign);
        response.setType(WeCrossType.PROPOSAL_TYPE_ENDORSER_PAYLODAD);
        return response;
    }

    private com.webank.wecross.restserver.response.ProposalResponse generateOrdererProposal(
            ProposalRequest request) throws Exception {
        com.webank.wecross.restserver.response.ProposalResponse response =
                new com.webank.wecross.restserver.response.ProposalResponse();
        response.setSeq(request.getSeq());
        response.setCryptoSuite(getCryptoSuite());

        byte[] bytesToSign = generateOrdererProposalSignBytes(request);
        response.setErrorCode(0);
        response.setProposalToSign(bytesToSign);
        response.setType(WeCrossType.PROPOSAL_TYPE_ORDERER_PAYLOAD);
        return response;
    }

    private byte[] generateEndorserProposalSignBytes(ProposalRequest request) throws Exception {

        Proposal proposal = proposalFactory.build(request);
        return proposal.getBytesToSign();
    }

    private byte[] generateOrdererProposalSignBytes(ProposalRequest request) throws Exception {
        byte[] proposalBytes = request.getExtraData();
        FabricProposal proposal = proposalFactory.buildFromBytes(proposalBytes);

        if (!proposal.isEqualsRequest(request)) {
            throw new WeCrossException(
                    ResourceQueryStatus.INVALID_PROPOSAL_BYTES,
                    "Proposal bytes is not belongs to this request");
        }

        Collection<ProposalResponse> transactionPropResp =
                sendFabricProposalRequest(proposal, request.getExtraSig());

        for (ProposalResponse response : transactionPropResp) {
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                logger.info(
                        "[√] Got success response from peer:{} , payload:{}",
                        response.getPeer().getName(),
                        new String(response.getChaincodeActionResponsePayload()));
            } else {

                String status = response.getStatus().toString();
                logger.error(
                        "[×] Got failed response from peer:{}, status:{}, error message:{}",
                        response.getPeer().getName(),
                        status,
                        response.getMessage());
            }
        }

        return FabricInnerProposalResponsesEncoder.encode(transactionPropResp);
    }

    private Collection<ProposalResponse> sendFabricProposalRequest(
            FabricProposal proposal, byte[] sign) throws Exception {
        Channel fabricChannel = fabricConn.getChannel();

        Collection<Peer> fabricPeer =
                fabricChannel.getPeers(EnumSet.of(Peer.PeerRole.CHAINCODE_QUERY));
        org.hyperledger.fabric.protos.peer.FabricProposal.SignedProposal sp =
                org.hyperledger.fabric.protos.peer.FabricProposal.SignedProposal.newBuilder()
                        .setProposalBytes(proposal.getInnerFabricProposal().toByteString())
                        .setSignature(ByteString.copyFrom(sign))
                        .build();

        TransactionContext transactionContext = proposalFactory.getTransactionContext(proposal);

        return (Collection<ProposalResponse>)
                methodSendProposalToPeers.invoke(
                        (Object) fabricChannel, fabricPeer, sp, transactionContext);
    }

    private TransactionResponse sendFabricOrdererRequest(TransactionRequest request)
            throws Exception {

        // New response
        TransactionResponse transactionResponse = buildTransactionResponse(request.getRetTypes());
        if (transactionResponse.getErrorCode() != 0) {
            return transactionResponse;
        }

        byte[] payload = request.getProposalBytes();
        byte[] sign = request.getSig();
        final String proposalTransactionID = getTxIDFromProposalBytes(payload);

        // Encode envelop to send
        Common.Envelope envelope =
                Common.Envelope.newBuilder()
                        .setPayload(ByteString.copyFrom(payload))
                        .setSignature(ByteString.copyFrom(sign))
                        .build();

        // Send and wait response
        CompletableFuture<TransactionEvent> future =
                sendOrdererPayload(envelope, proposalTransactionID);

        long transactionTimeout = 5000;
        try {
            BlockEvent.TransactionEvent transactionEvent =
                    future.get(transactionTimeout, TimeUnit.MILLISECONDS);
            if (transactionEvent.isValid()) {
                BlockchainInfo channelInfo = fabricConn.getChannel().queryBlockchainInfo();
                transactionResponse.setHash(Hex.encodeHexString(channelInfo.getCurrentBlockHash()));
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
                transactionResponse.setErrorCode(ResourceQueryStatus.FABRIC_COMMIT_CHAINCODE_FAIL);
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
            transactionResponse.setErrorCode(ResourceQueryStatus.FABRIC_INVOKE_CHAINCODE_FAIL);
            return transactionResponse;
        }

        // get return value
        List<Object> resultList = new ArrayList<Object>();

        resultList.add(getResponsePayload(Common.Payload.parseFrom(payload)));
        transactionResponse.setResult(resultList.toArray());
        return transactionResponse;
    }

    private String getResponsePayload(Common.Payload payload) throws Exception {
        FabricTransaction.Transaction tx =
                FabricTransaction.Transaction.parseFrom(payload.getData());
        FabricTransaction.TransactionAction action = tx.getActions(0);
        FabricTransaction.ChaincodeActionPayload chaincodeActionPayload =
                FabricTransaction.ChaincodeActionPayload.parseFrom(action.getPayload());
        FabricTransaction.ChaincodeEndorsedAction chaincodeEndorsedAction =
                chaincodeActionPayload.getAction();

        FabricProposalResponse.ProposalResponsePayload proposalResponsePayload =
                FabricProposalResponse.ProposalResponsePayload.parseFrom(
                        chaincodeEndorsedAction.getProposalResponsePayload());
        org.hyperledger.fabric.protos.peer.FabricProposal.ChaincodeAction chaincodeAction =
                org.hyperledger.fabric.protos.peer.FabricProposal.ChaincodeAction.parseFrom(
                        proposalResponsePayload.getExtension());
        byte[] ret = chaincodeAction.getResponse().getPayload().toByteArray();
        byte[] ret2 = proposalResponsePayload.toByteArray();

        return new String(ret);
    }

    private CompletableFuture<TransactionEvent> createTransactionEvent(
            String proposalTransactionID) {
        try {
            Channel channel = getFabricConn().getChannel();
            String name = channel.getName();
            Channel.NOfEvents nOfEvents = Channel.NOfEvents.createNofEvents();
            Collection<Peer> eventingPeers =
                    channel.getPeers(EnumSet.of(Peer.PeerRole.EVENT_SOURCE));
            boolean anyAdded = false;
            if (!eventingPeers.isEmpty()) {
                anyAdded = true;
                nOfEvents.addPeers(eventingPeers);
            }
            Collection<EventHub> eventHubs = channel.getEventHubs();
            if (!eventHubs.isEmpty()) {
                anyAdded = true;
                nOfEvents.addEventHubs(channel.getEventHubs());
            }

            if (!anyAdded) {
                nOfEvents = Channel.NOfEvents.createNoEvents();
            }

            final boolean replyonly =
                    nOfEvents == Channel.NOfEvents.nofNoEvents
                            || (channel.getEventHubs().isEmpty()
                                    && channel.getPeers(EnumSet.of(Peer.PeerRole.EVENT_SOURCE))
                                            .isEmpty());

            CompletableFuture<TransactionEvent> sret;

            if (replyonly) { // If there are no eventhubs to complete the future, complete it
                // immediately but give no transaction event
                logger.debug(
                        format(
                                "Completing transaction id %s immediately no event hubs or peer eventing services found in channel %s.",
                                proposalTransactionID, name));
                sret = new CompletableFuture<>();
            } else {
                sret =
                        (CompletableFuture<TransactionEvent>)
                                methodRegisterTxListener.invoke(
                                        (Object) channel, proposalTransactionID, nOfEvents, true);
            }

            return sret;
        } catch (Exception e) {

            CompletableFuture<TransactionEvent> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    private CompletableFuture<TransactionEvent> sendOrdererPayload(
            Common.Envelope transactionEnvelope, String proposalTransactionID) throws Exception {
        // make certain we have our own copy
        Channel channel = getFabricConn().getChannel();

        final List<Orderer> shuffeledOrderers = new ArrayList<>(channel.getOrderers());
        final String name = channel.getName();
        Channel.TransactionOptions transactionOptions =
                Channel.TransactionOptions.createTransactionOptions()
                        .orderers(channel.getOrderers())
                        .userContext(getFabricConn().getHfClient().getUserContext());
        try {
            Collections.shuffle(shuffeledOrderers);

            logger.debug(
                    format(
                            "Channel %s sending transaction to orderer(s) with TxID %s ",
                            name, proposalTransactionID));
            boolean success = false;
            Exception lException =
                    null; // Save last exception to report to user .. others are just logged.

            CompletableFuture<TransactionEvent> sret =
                    createTransactionEvent(proposalTransactionID);

            Ab.BroadcastResponse resp = null;
            Orderer failed = null;

            for (Orderer orderer : shuffeledOrderers) {
                if (failed != null) {
                    logger.warn(
                            format("Channel %s  %s failed. Now trying %s.", name, failed, orderer));
                }
                failed = orderer;
                try {

                    resp =
                            (Ab.BroadcastResponse)
                                    methodSendTransactionToOrderer.invoke(
                                            (Object) orderer, transactionEnvelope);

                    lException = null; // no longer last exception .. maybe just failed.
                    if (resp.getStatus() == Common.Status.SUCCESS) {
                        success = true;
                        break;
                    } else {
                        logger.warn(
                                format(
                                        "Channel %s %s failed. Status returned %s",
                                        name, orderer, dumpRespData(resp)));
                    }
                } catch (Exception e) {
                    String emsg =
                            format(
                                    "Channel %s unsuccessful sendTransaction to orderer %s (%s)",
                                    name, orderer.getName(), orderer.getUrl());
                    if (resp != null) {

                        emsg =
                                format(
                                        "Channel %s unsuccessful sendTransaction to orderer %s (%s).  %s",
                                        name,
                                        orderer.getName(),
                                        orderer.getUrl(),
                                        dumpRespData(resp));
                    }

                    logger.error(emsg);
                    lException = new Exception(emsg, e);
                }
            }

            if (success) {
                logger.debug(
                        format(
                                "Channel %s successful sent to Orderer transaction id: %s",
                                name, proposalTransactionID));

                // sret.complete(null); // just say we're done.

                return sret;
            } else {

                String emsg =
                        format(
                                "Channel %s failed to place transaction %s on Orderer. Cause: UNSUCCESSFUL. %s",
                                name, proposalTransactionID, dumpRespData(resp));

                CompletableFuture<TransactionEvent> ret = new CompletableFuture<>();
                ret.completeExceptionally(
                        lException != null ? new Exception(emsg, lException) : new Exception(emsg));
                return ret;
            }
        } catch (Exception e) {

            CompletableFuture<TransactionEvent> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    private String dumpRespData(Ab.BroadcastResponse resp) {

        StringBuilder respdata = new StringBuilder(400);
        if (resp != null) {
            Common.Status status = resp.getStatus();
            if (null != status) {
                respdata.append(status.name());
                respdata.append("-");
                respdata.append(status.getNumber());
            }

            String info = resp.getInfo();
            if (null != info && !info.isEmpty()) {
                if (respdata.length() > 0) {
                    respdata.append(", ");
                }
                respdata.append("Additional information: ").append(info);
            }
        }

        return respdata.toString();
    }

    private String getTxIDFromProposalBytes(byte[] proposalBytes) throws Exception {
        Common.Payload payload = Common.Payload.parseFrom(proposalBytes);

        Common.ChannelHeader channelHeader =
                Common.ChannelHeader.parseFrom(payload.getHeader().getChannelHeader());
        return channelHeader.getTxId();
    }
}
