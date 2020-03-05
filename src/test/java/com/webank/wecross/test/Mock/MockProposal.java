package com.webank.wecross.test.Mock;

import com.webank.wecross.proposal.Proposal;
import com.webank.wecross.restserver.request.ProposalRequest;
import com.webank.wecross.restserver.request.TransactionRequest;

public class MockProposal extends Proposal {
    public MockProposal(int seq) {
        super(seq);
    }

    @Override
    public byte[] getBytesToSign() {
        return null;
    }

    @Override
    public byte[] getBytes() {
        return new byte[0];
    }

    public MockProposal(int seq, Long deadline) {
        super(seq);
        forceDeadline(deadline);
    }

    @Override
    public void sendSignedPayload(byte[] signBytes) throws Exception {
        return;
    }

    @Override
    public void loadBytes(byte[] proposalBytes) throws Exception {}

    @Override
    public Boolean isEqualsRequest(TransactionRequest request) throws Exception {
        return null;
    }

    @Override
    public Boolean isEqualsRequest(ProposalRequest request) throws Exception {
        return null;
    }
}
