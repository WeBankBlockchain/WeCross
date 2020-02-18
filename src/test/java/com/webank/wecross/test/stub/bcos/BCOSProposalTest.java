package com.webank.wecross.test.stub.bcos;

import com.webank.wecross.proposal.Proposal;
import com.webank.wecross.stub.bcos.BCOSProposal;
import com.webank.wecross.stub.bcos.BCOSProposalFactory;
import com.webank.wecross.test.Mock.MockTransactionRequest;
import com.webank.wecross.test.Mock.web3j.MockWeb3j;
import java.util.Arrays;
import org.fisco.bcos.web3j.crypto.ExtendedRawTransaction;
import org.fisco.bcos.web3j.crypto.ExtendedTransactionEncoder;
import org.junit.Assert;
import org.junit.Test;

public class BCOSProposalTest {
    @Test
    public void allTest() throws Exception {
        BCOSProposalFactory factory = new BCOSProposalFactory("0xaaabbbccc", new MockWeb3j(), null);
        MockTransactionRequest req = new MockTransactionRequest();

        Proposal proposalA = factory.build(req.toProposalPrequest());
        Assert.assertTrue(proposalA.isEqualsRequest(req));

        byte[] bytesA = proposalA.getBytes();

        BCOSProposal proposalB = new BCOSProposal(0);
        proposalB.loadBytes(bytesA);

        byte[] bytesB = proposalB.getBytes();
        Assert.assertTrue(Arrays.equals(bytesA, bytesB));

        ExtendedRawTransaction innerBCOSTransactionA =
                ((BCOSProposal) proposalA).getInnerBCOSTransaction();
        ExtendedRawTransaction innerBCOSTransactionB = proposalB.getInnerBCOSTransaction();
        bytesA = ExtendedTransactionEncoder.encode(innerBCOSTransactionA);
        bytesB = ExtendedTransactionEncoder.encode(innerBCOSTransactionB);
        Assert.assertTrue(Arrays.equals(bytesA, bytesB));

        Assert.assertTrue(proposalB.isEqualsRequest(req));
    }
}
