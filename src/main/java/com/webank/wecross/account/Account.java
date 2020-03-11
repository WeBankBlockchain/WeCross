package com.webank.wecross.account;

public interface Account {
    byte[] reassembleProposal(byte[] proposalBytes, String proposalType) throws Exception;

    Boolean isProposalReady(String proposalType);

    byte[] sign(byte[] message) throws Exception;

    String getName();

    String getAddress();

    String getSignCryptoSuite();
}
