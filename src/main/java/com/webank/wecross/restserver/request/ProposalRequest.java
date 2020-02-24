package com.webank.wecross.restserver.request;

public class ProposalRequest {
    private int seq;
    private String method;
    private Object args[];
    private byte[] extraData;
    private byte[] extraSig;

    public ProposalRequest() {}

    public ProposalRequest(int seq, String method, Object[] args) {
        this.seq = seq;
        this.method = method;
        this.args = args;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object args[]) {
        this.args = args;
    }

    public byte[] getExtraData() {
        return extraData;
    }

    public void setExtraData(byte[] extraData) {
        this.extraData = extraData;
    }

    public byte[] getExtraSig() {
        return extraSig;
    }

    public void setExtraSig(byte[] extraSig) {
        this.extraSig = extraSig;
    }
}
