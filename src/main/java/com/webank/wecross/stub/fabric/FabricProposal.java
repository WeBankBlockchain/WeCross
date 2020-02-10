package com.webank.wecross.stub.fabric;

import static com.webank.wecross.stub.fabric.FabricContractResource.getParamterList;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.protobuf.ByteString;
import com.webank.wecross.proposal.Proposal;
import com.webank.wecross.restserver.request.TransactionRequest;
import java.util.ArrayList;
import java.util.List;
import org.hyperledger.fabric.protos.peer.Chaincode;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;

public class FabricProposal extends Proposal {
    private byte[] proposalBytes;
    private org.hyperledger.fabric.protos.peer.FabricProposal.Proposal innerFabricProposal;

    public FabricProposal(int seq) {
        super(seq);
    }

    @Override
    public byte[] getBytesToSign() {
        return proposalBytes;
    }

    @Override
    public byte[] getBytes() {
        return proposalBytes;
    }

    public org.hyperledger.fabric.protos.peer.FabricProposal.Proposal getInnerFabricProposal() {
        return this.innerFabricProposal;
    }

    @Override
    public void sendSignedPayload(byte[] signBytes) throws Exception {}

    @Override
    public void loadBytes(byte[] proposalBytes) throws Exception {
        this.proposalBytes = proposalBytes;
        this.innerFabricProposal =
                org.hyperledger.fabric.protos.peer.FabricProposal.Proposal.parseFrom(proposalBytes);
    }

    public void load(
            org.hyperledger.fabric.protos.peer.FabricProposal.Proposal innerFabricProposal) {
        this.innerFabricProposal = innerFabricProposal;
        this.proposalBytes = innerFabricProposal.toByteArray();
    }

    @Override
    public Boolean isEqualsRequest(TransactionRequest request) throws Exception {
        org.hyperledger.fabric.protos.peer.Chaincode.ChaincodeInput givenInput =
                encodeRequestToInputData(request);
        org.hyperledger.fabric.protos.peer.Chaincode.ChaincodeInput originInput =
                getProposalInput(innerFabricProposal);

        return givenInput.equals(originInput);
    }

    private org.hyperledger.fabric.protos.peer.Chaincode.ChaincodeInput encodeRequestToInputData(
            TransactionRequest request) throws Exception {
        TransactionProposalRequest transactionProposalRequest =
                TransactionProposalRequest.newInstance(null);
        transactionProposalRequest.setFcn(request.getMethod());
        String[] paramterList = getParamterList(request);
        transactionProposalRequest.setArgs(paramterList);

        // From Fabric 1.4 ProposalBuilder
        List<ByteString> allArgs = new ArrayList<>();

        // if argList is empty and we have a Request, build the chaincodeInput args array from the
        // Request args and argbytes lists
        allArgs.add(ByteString.copyFrom(transactionProposalRequest.getFcn(), UTF_8));
        List<String> args = transactionProposalRequest.getArgs();
        if (args != null && args.size() > 0) {
            for (String arg : args) {
                allArgs.add(ByteString.copyFrom(arg.getBytes(UTF_8)));
            }
        }
        // TODO currently assume that chaincodeInput args are strings followed by byte[].
        // Either agree with Fabric folks that this will always be the case or modify all Builders
        // to expect
        // a List of Objects and determine if each list item is a string or a byte array
        List<byte[]> argBytes = transactionProposalRequest.getArgBytes();
        if (argBytes != null && argBytes.size() > 0) {
            for (byte[] arg : argBytes) {
                allArgs.add(ByteString.copyFrom(arg));
            }
        }

        return Chaincode.ChaincodeInput.newBuilder().addAllArgs(allArgs).build();
    }

    private org.hyperledger.fabric.protos.peer.Chaincode.ChaincodeInput getProposalInput(
            org.hyperledger.fabric.protos.peer.FabricProposal.Proposal fabricProposal)
            throws Exception {
        org.hyperledger.fabric.protos.peer.FabricProposal.ChaincodeProposalPayload payload =
                org.hyperledger.fabric.protos.peer.FabricProposal.ChaincodeProposalPayload
                        .parseFrom(fabricProposal.getPayload());

        Chaincode.ChaincodeInvocationSpec chaincodeInvocationSpec =
                Chaincode.ChaincodeInvocationSpec.parseFrom(payload.getInput());

        Chaincode.ChaincodeSpec chaincodeSpec = chaincodeInvocationSpec.getChaincodeSpec();

        org.hyperledger.fabric.protos.peer.Chaincode.ChaincodeInput chaincodeInput =
                chaincodeSpec.getInput();
        return chaincodeInput;
    }
}
