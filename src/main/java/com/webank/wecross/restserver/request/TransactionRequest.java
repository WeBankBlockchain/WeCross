package com.webank.wecross.restserver.request;

import java.util.Arrays;

public class TransactionRequest {

    private int seq = 0;
    private byte[] sig;
    private byte[] proposalBytes;
    private String retTypes[];
    private String method;
    private Object args[];

    public byte[] getSig() {
        return sig;
    }

    public void setSig(byte[] sig) {
        this.sig = sig;
    }

    public String[] getRetTypes() {
        return retTypes;
    }

    public void setRetTypes(String[] retTypes) {
        this.retTypes = retTypes;
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

    @Override
    public String toString() {
        return "TransactionRequest{"
                + "sig='"
                + sig
                + '\''
                + ", retTypes="
                + Arrays.toString(retTypes)
                + ", method='"
                + method
                + '\''
                + ", args="
                + Arrays.toString(args)
                + '}';
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public byte[] getProposalBytes() {
        return proposalBytes;
    }

    public void setProposalBytes(byte[] proposalBytes) {
        this.proposalBytes = proposalBytes;
    }
}
