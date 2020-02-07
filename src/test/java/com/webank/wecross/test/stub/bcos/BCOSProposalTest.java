package com.webank.wecross.test.stub.bcos;

import com.webank.wecross.proposal.Proposal;
import com.webank.wecross.restserver.request.TransactionRequest;
import com.webank.wecross.stub.bcos.BCOSProposal;
import com.webank.wecross.stub.bcos.BCOSProposalFactory;
import com.webank.wecross.test.Mock.MockTransactionRequest;
import com.webank.wecross.test.Mock.web3j.MockWeb3j;
import org.junit.Assert;
import org.junit.Test;

public class BCOSProposalTest {
    @Test
    public void allTest() throws Exception {
        BCOSProposalFactory factory = new BCOSProposalFactory("0xaaabbbccc", new MockWeb3j(), null);
        TransactionRequest req = new MockTransactionRequest();

        Proposal proposalA = factory.build(req);
        Assert.assertTrue(proposalA.isEqualsRequest(req));

        byte[] bytesA = proposalA.getBytes();

        BCOSProposal proposalB = new BCOSProposal(0);
        proposalB.loadBytes(bytesA);

        byte[] bytesB = proposalB.getBytes();
        Assert.assertEquals(bytesA, bytesB);

        Assert.assertTrue(proposalB.isEqualsRequest(req));
    }
}
