package com.webank.wecross.stub.fabric;

import static com.webank.wecross.stub.fabric.FabricContractResource.getParamterList;

import com.google.protobuf.ByteString;
import com.webank.wecross.proposal.ProposalFactory;
import com.webank.wecross.restserver.request.ProposalRequest;
import org.hyperledger.fabric.protos.common.Common;
import org.hyperledger.fabric.protos.msp.Identities;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.helper.Utils;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.transaction.ProposalBuilder;
import org.hyperledger.fabric.sdk.transaction.TransactionContext;

public class FabricProposalFactory implements ProposalFactory {
    private FabricConn fabricConn;

    public FabricProposalFactory(FabricConn fabricConn) {
        this.fabricConn = fabricConn;
    }

    @Override
    public FabricProposal build(ProposalRequest request) throws Exception {
        // Build inner fabric proposal struct
        TransactionProposalRequest transactionProposalRequest =
                fabricConn.getHfClient().newTransactionProposalRequest();
        transactionProposalRequest.setChaincodeID(fabricConn.getChaincodeID());
        transactionProposalRequest.setChaincodeLanguage(fabricConn.getChainCodeType());
        transactionProposalRequest.setFcn(request.getMethod());
        String[] paramterList = getParamterList(request);
        transactionProposalRequest.setArgs(paramterList);
        transactionProposalRequest.setProposalWaitTime(fabricConn.getProposalWaitTime());

        org.hyperledger.fabric.sdk.TransactionRequest proposalRequest = transactionProposalRequest;

        TransactionContext transactionContext = getTransactionContext();
        transactionContext.verify(proposalRequest.doVerify());
        transactionContext.setProposalWaitTime(proposalRequest.getProposalWaitTime());

        ProposalBuilder proposalBuilder = ProposalBuilder.newBuilder();
        proposalBuilder.context(transactionContext);
        proposalBuilder.request(proposalRequest);

        // Build WeCross Fabric proposal
        FabricProposal proposal = new FabricProposal(request.getSeq());
        proposal.load(proposalBuilder.build());

        return proposal;
    }

    @Override
    public FabricProposal buildFromBytes(byte[] proposalBytes) throws Exception {
        FabricProposal proposal = new FabricProposal(0);
        proposal.loadBytes(proposalBytes);
        return proposal;
    }

    public TransactionContext getTransactionContext() throws Exception {
        User userContext = fabricConn.getHfClient().getUserContext();
        Channel channel = fabricConn.getChannel();
        HFClient client = fabricConn.getHfClient();
        userContext = userContext != null ? userContext : client.getUserContext();

        User.userContextCheck(userContext);

        TransactionContext transactionContext =
                new TransactionContext(channel, userContext, client.getCryptoSuite());

        return transactionContext;
    }

    public TransactionContext getTransactionContext(FabricProposal proposal) throws Exception {
        User userContext = fabricConn.getHfClient().getUserContext();
        Channel channel = fabricConn.getChannel();
        HFClient client = fabricConn.getHfClient();
        userContext = userContext != null ? userContext : client.getUserContext();

        User.userContextCheck(userContext);

        String txID = recoverTxID(proposal);

        TransactionContext transactionContext =
                new TransactionContextMask(txID, channel, userContext, client.getCryptoSuite());

        return transactionContext;
    }

    private String recoverTxID(FabricProposal proposal) throws Exception {
        org.hyperledger.fabric.protos.peer.FabricProposal.Proposal innerFabricProposal =
                proposal.getInnerFabricProposal();

        Common.Header header = Common.Header.parseFrom(innerFabricProposal.getHeader());

        Common.SignatureHeader signatureHeader =
                Common.SignatureHeader.parseFrom(header.getSignatureHeader());

        Identities.SerializedIdentity serializedIdentity =
                Identities.SerializedIdentity.parseFrom(signatureHeader.getCreator());

        ByteString no = signatureHeader.getNonce();

        ByteString comp = no.concat(serializedIdentity.toByteString());

        byte[] txh = CryptoSuite.Factory.getCryptoSuite().hash(comp.toByteArray());

        //    txID = Hex.encodeHexString(txh);
        String txID = new String(Utils.toHexString(txh));
        return txID;
    }

    // Only for mask the txID
    public static class TransactionContextMask extends TransactionContext {
        private String txIDMask;
        private byte[] signMask = null;

        public TransactionContextMask(
                String txID, Channel channel, User user, CryptoSuite cryptoPrimitives) {
            super(channel, user, cryptoPrimitives);
            this.txIDMask = txID;
        }

        @Override
        public byte[] sign(byte[] b) throws CryptoException, InvalidArgumentException {
            if (signMask != null) {
                return signMask;
            }
            return super.sign(b);
        }

        @Override
        public String getTxID() {
            return txIDMask;
        }

        public void setSignMask(byte[] signMask) {
            this.signMask = signMask;
        }
    }
}
