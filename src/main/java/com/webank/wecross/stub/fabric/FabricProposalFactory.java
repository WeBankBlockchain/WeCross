package com.webank.wecross.stub.fabric;

import static com.webank.wecross.stub.fabric.FabricContractResource.getParamterList;

import com.webank.wecross.proposal.ProposalFactory;
import com.webank.wecross.restserver.request.ProposalRequest;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.User;
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

        TransactionContext transactionContext =
                getTransactionContext(fabricConn.getHfClient().getUserContext());
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

    private TransactionContext getTransactionContext(User userContext) throws Exception {
        Channel channel = fabricConn.getChannel();
        HFClient client = fabricConn.getHfClient();
        userContext = userContext != null ? userContext : client.getUserContext();

        User.userContextCheck(userContext);

        return new TransactionContext(channel, userContext, client.getCryptoSuite());
    }
}
