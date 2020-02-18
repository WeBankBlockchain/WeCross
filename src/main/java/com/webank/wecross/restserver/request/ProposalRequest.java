package com.webank.wecross.restserver.request;

public class ProposalRequest {
    private int seq;
    private String method;
    private Object args[];

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
}
