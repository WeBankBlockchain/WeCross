package com.webank.wecross.stub;

import java.util.Arrays;

public class TransactionRequest {
    private int seq = 0;
    private String method;
    private String[] args;
    private boolean fromP2P;

    public TransactionRequest() {}

    public TransactionRequest(String method, String[] args) {
        this.method = method;
        this.args = args;
    }

    public TransactionRequest(String method, String[] args, boolean fromP2P) {
        this.method = method;
        this.args = args;
        this.fromP2P = fromP2P;
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

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public boolean isFromP2P() {
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
                + '\''
                + ", method='"
                + method
                + '\''
                + ", args="
                + Arrays.toString(args)
                + ", fromP2P="
                + fromP2P
                + '}';
    }
}
