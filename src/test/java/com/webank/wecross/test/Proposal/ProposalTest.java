package com.webank.wecross.test.Proposal;

import com.webank.wecross.proposal.Proposal;
import com.webank.wecross.test.Mock.MockProposal;
import org.junit.Assert;
import org.junit.Test;

public class ProposalTest {
    @Test
    public void seqTest() {
        Proposal proposal = new MockProposal(666);
        Assert.assertEquals(proposal.getSeq(), 666);
    }

    @Test
    public void timeoutTest() throws Exception {
        Proposal proposal = new MockProposal(666, System.currentTimeMillis() + 500);
        Assert.assertFalse(proposal.isTimeout());
        Thread.sleep(550);
        Assert.assertTrue(proposal.isTimeout());
    }
}
