package com.webank.wecross.test.Mock;

import com.webank.wecross.proposal.Proposal;

public class MockProposal extends Proposal {
    public MockProposal(int seq) {
        super(seq);
    }

    public MockProposal(int seq, Long deadline) {
        super(seq);
        forceDeadline(deadline);
    }

    @Override
    public void sendSignedPayload(String signature) throws Exception {
        return;
    }
}
