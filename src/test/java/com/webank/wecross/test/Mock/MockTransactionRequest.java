package com.webank.wecross.test.Mock;

import com.webank.wecross.restserver.request.ProposalRequest;
import com.webank.wecross.restserver.request.TransactionRequest;

public class MockTransactionRequest extends TransactionRequest {
    public MockTransactionRequest() {
        this.setMethod("set");
        this.setArgs(new Object[] {"aaa", 123});
        this.setRetTypes(new String[] {"String"});
    }

    public ProposalRequest toProposalPrequest() {
        return new ProposalRequest(100, getMethod(), getArgs());
    }
}
