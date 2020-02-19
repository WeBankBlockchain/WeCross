package com.webank.wecross.restserver.request;

import java.util.Arrays;

public class TransactionRequest {

    private int seq = 0;
    private byte[] sig;
    private byte[] proposalBytes;
    private String retTypes[];
    private String method;
    private Object args[];
    private boolean fromP2P;

    public TransactionRequest() {}

    public TransactionRequest(String[] retTypes, String method, Object[] args) {
        this.retTypes = retTypes;
        this.method = method;
        this.args = args;
    }

    public TransactionRequest(String[] retTypes, String method, Object[] args, boolean fromP2P) {
        this.retTypes = retTypes;
        this.method = method;
        this.args = args;
        this.fromP2P = fromP2P;
    }

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

    public boolean getFromP2P() {
        return fromP2P;
    }

    public void setFromP2P(boolean fromP2P) {
        this.fromP2P = fromP2P;
    }

    @Override
    public String toString() {
        return "TransactionRequest{"
                + "seq="
                + seq
                + ", sig="
                + Arrays.toString(sig)
                + ", proposalBytes="
                + Arrays.toString(proposalBytes)
                + ", retTypes="
                + Arrays.toString(retTypes)
                + ", method='"
                + method
                + '\''
                + ", args="
                + Arrays.toString(args)
                + ", fromP2P="
                + fromP2P
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
