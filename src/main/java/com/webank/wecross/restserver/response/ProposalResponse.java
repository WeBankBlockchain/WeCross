package com.webank.wecross.restserver.response;

public class ProposalResponse {
    private int errorCode;
    private String errorMessage;
    private int seq;
    private byte[] proposalToSign;
    private String cryptoSuite;
    private String type;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public byte[] getProposalToSign() {
        return proposalToSign;
    }

    public void setProposalToSign(byte[] proposalToSign) {
        this.proposalToSign = proposalToSign;
    }

    public String getCryptoSuite() {
        return cryptoSuite;
    }

    public void setCryptoSuite(String cryptoSuite) {
        this.cryptoSuite = cryptoSuite;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
