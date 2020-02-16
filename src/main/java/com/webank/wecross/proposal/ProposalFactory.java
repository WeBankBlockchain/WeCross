package com.webank.wecross.proposal;

import com.webank.wecross.restserver.request.ProposalRequest;

public interface ProposalFactory {
    Proposal build(ProposalRequest request) throws Exception;
}
