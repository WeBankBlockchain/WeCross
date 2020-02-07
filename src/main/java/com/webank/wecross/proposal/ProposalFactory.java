package com.webank.wecross.proposal;

import com.webank.wecross.restserver.request.TransactionRequest;

public interface ProposalFactory {
    Proposal build(TransactionRequest request) throws Exception;
}
