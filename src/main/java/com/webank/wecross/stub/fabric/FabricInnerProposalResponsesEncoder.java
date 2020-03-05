package com.webank.wecross.stub.fabric;

import static java.lang.String.format;

import com.google.protobuf.ByteString;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.hyperledger.fabric.protos.common.Common;
import org.hyperledger.fabric.protos.peer.FabricProposalResponse;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.transaction.TransactionBuilder;

public class FabricInnerProposalResponsesEncoder {

    public static byte[] encode(Collection<ProposalResponse> proposalResponses) throws Exception {
        List<FabricProposalResponse.Endorsement> ed = new LinkedList<>();
        org.hyperledger.fabric.protos.peer.FabricProposal.Proposal proposal = null;
        ByteString proposalResponsePayload = null;
        String proposalTransactionID = null;

        for (ProposalResponse sdkProposalResponse : proposalResponses) {

            // Ignore failed cases
            if (sdkProposalResponse.getStatus() != ProposalResponse.Status.SUCCESS) {
                continue;
            }

            ed.add(sdkProposalResponse.getProposalResponse().getEndorsement());
            if (proposal == null) {
                proposal = sdkProposalResponse.getProposal();
                proposalTransactionID = sdkProposalResponse.getTransactionID();
                if (proposalTransactionID == null) {
                    throw new InvalidArgumentException("Proposals with missing transaction ID");
                }
                proposalResponsePayload = sdkProposalResponse.getProposalResponse().getPayload();
                if (proposalResponsePayload == null) {
                    throw new InvalidArgumentException("Proposals with missing payload.");
                }
            } else {
                final String transactionID = sdkProposalResponse.getTransactionID();
                if (transactionID == null) {
                    throw new InvalidArgumentException("Proposals with missing transaction id.");
                }
                if (!proposalTransactionID.equals(transactionID)) {
                    throw new InvalidArgumentException(
                            format(
                                    "Proposals with different transaction IDs %s,  and %s",
                                    proposalTransactionID, transactionID));
                }
            }
        }

        TransactionBuilder transactionBuilder = TransactionBuilder.newBuilder();

        Common.Payload transactionPayload =
                transactionBuilder
                        .chaincodeProposal(proposal)
                        .endorsements(ed)
                        .proposalResponsePayload(proposalResponsePayload)
                        .build();

        return transactionPayload.toByteArray();
    }
    /*
        public static Collection<ProposalResponse> decode(byte[] bytes, byte[] sign, FabricConn fabricConn) throws Exception {

            // sdkProposalResponse.getProposalResponse().payload
            // ed: sdkProposalResponse.getProposalResponse().getEndorsement()
            // ByteString:  sdkProposalResponse.getProposal()
            // sdkProposalResponse.getTransactionID()
            // sdkProposalResponse.getTransactionContext()
            // getTransactionContext.sign()

            Common.Payload payload = Common.Payload.parseFrom(bytes);
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

            // sdkProposalResponse.getProposalResponse().payload
            proposalResponsePayload.toByteString();

            // eds: ed: sdkProposalResponse.getProposalResponse().getEndorsement()
            List<FabricProposalResponse.Endorsement> eds = chaincodeEndorsedAction.getEndorsementsList();

            // ByteString:  sdkProposalResponse.getProposal()
            org.hyperledger.fabric.protos.peer.FabricProposal.ChaincodeProposalPayload chaincodeProposalPayload = org.hyperledger.fabric.protos.peer.FabricProposal.ChaincodeProposalPayload.parseFrom(chaincodeActionPayload.getChaincodeProposalPayload());
            FabricProposal.Proposal proposal = FabricProposal.Proposal.newBuilder()
                    .setHeader(payload.getHeader().toByteString())
                    .setPayload(chaincodeProposalPayload.toByteString()) // need merge from?
                    .build();

            // sdkProposalResponse.getTransactionID()
            Common.ChannelHeader channelHeader =
                    Common.ChannelHeader.parseFrom(payload.getHeader().getChannelHeader());
            String txID = channelHeader.getTxId();

            // sdkProposalResponse.getTransactionContext()
            User userContext = fabricConn.getHfClient().getUserContext();
            Channel channel = fabricConn.getChannel();
            HFClient client = fabricConn.getHfClient();
            TransactionContext transactionContext = new FabricProposalFactory.TransactionContextMask(txID, channel, userContext, client.getCryptoSuite());

            Collection<ProposalResponse> proposalResponses = new ArrayList<>();
            for(FabricProposalResponse.Endorsement ed : eds) {

                ProposalResponse.class.newInstance(transactionContext, ChaincodeResponse.Status.SUCCESS, "");
                ProposalResponse proposalResponse = new ProposalResponse(transactionContext, 500, "");
                proposalResponse.setProposalResponse(fabricResponse);
                proposalResponse.setProposal(signedProposal);
                proposalResponse.setPeer(peerFuturePair.peer);

                if (fabricResponse != null && transactionContext.getVerify()) {
                    proposalResponse.verify(client.getCryptoSuite());
                }

                proposalResponses.add(proposalResponse);
            }

        }

        public static class ProposalResponseMask extends ProposalResponse {
            @Override
            ProposalResponse(TransactionContext transactionContext, int status, String message){

            }

            public ProposalResponseMask(TransactionContext transactionContext){
                super(transactionContext, Status.SUCCESS, "");
            }
        }
    */
}
