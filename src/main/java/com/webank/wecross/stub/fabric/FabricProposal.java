package com.webank.wecross.stub.fabric;

import static com.webank.wecross.stub.fabric.FabricContractResource.getParamterList;

import com.webank.wecross.proposal.Proposal;
import com.webank.wecross.restserver.request.TransactionRequest;
import org.hyperledger.fabric.protos.peer.Chaincode;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.transaction.ProposalBuilder;

public class FabricProposal extends Proposal {
    byte[] proposalBytes;
    org.hyperledger.fabric.protos.peer.FabricProposal.Proposal innerFabricProposal;

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

    @Override
    public void sendSignedPayload(byte[] signBytes) throws Exception {}

    @Override
    public void loadBytes(byte[] proposalBytes) throws Exception {
        this.proposalBytes = proposalBytes;
        this.innerFabricProposal =
                org.hyperledger.fabric.protos.peer.FabricProposal.Proposal.parseFrom(proposalBytes);
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
        transactionProposalRequest.setProposalWaitTime(120000);

        ProposalBuilder proposalBuilder = ProposalBuilder.newBuilder();
        proposalBuilder.context(null);
        proposalBuilder.request(transactionProposalRequest);

        org.hyperledger.fabric.protos.peer.FabricProposal.Proposal proposal =
                proposalBuilder.build();

        return getProposalInput(proposal);
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
