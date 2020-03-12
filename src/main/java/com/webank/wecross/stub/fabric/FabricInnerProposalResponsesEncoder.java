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
}
