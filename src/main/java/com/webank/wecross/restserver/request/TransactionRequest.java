package com.webank.wecross.restserver.request;

import java.util.Arrays;

public class TransactionRequest {

    private int seq = 0;
    private String sig;
    private String retTypes[];
    private String method;
    private Object args[];

    public String getSig() {
        return sig;
    }

    public void setSig(String sig) {
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
}
