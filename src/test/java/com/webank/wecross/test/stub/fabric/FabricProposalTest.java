package com.webank.wecross.test.stub.fabric;

import com.webank.wecross.proposal.Proposal;
import com.webank.wecross.proposal.ProposalFactory;
import com.webank.wecross.stub.fabric.FabricProposal;
import com.webank.wecross.stub.fabric.FabricProposalFactory;
import com.webank.wecross.test.Mock.MockTransactionRequest;
import com.webank.wecross.test.Mock.fabric.MockFabricConn;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;

public class FabricProposalTest {
    @Test
    public void AllTest() throws Exception {
        ProposalFactory factory = new FabricProposalFactory(new MockFabricConn());

        MockTransactionRequest req = new MockTransactionRequest();

        Proposal proposalA = factory.build(req);
        Assert.assertTrue(proposalA.isEqualsRequest(req));

        byte[] bytesA = proposalA.getBytes();

        FabricProposal proposalB = new FabricProposal(0);
        proposalB.loadBytes(bytesA);

        byte[] bytesB = proposalB.getBytes();
        Assert.assertTrue(Arrays.equals(bytesA, bytesB));

        Assert.assertEquals(
                ((FabricProposal) proposalA).getInnerFabricProposal(),
                proposalB.getInnerFabricProposal());

        Assert.assertTrue(proposalB.isEqualsRequest(req));
    }
}
