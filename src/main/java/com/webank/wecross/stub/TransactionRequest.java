package com.webank.wecross.stub;

import java.util.Arrays;
import java.util.Map;

public class TransactionRequest {
    private int seq = 0;
    private String method;
    private String[] args;
    
    // Optional args
    // transactionID, peers, etc...
    private Map<String, String> options;

    public TransactionRequest() {}

    public TransactionRequest(String method, String[] args) {
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

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }
    
    public Map<String, String> getOptions() {
		return options;
	}

	public void setOptions(Map<String, String> options) {
		this.options = options;
	}

    @Override
    public String toString() {
        return "TransactionRequest{"
                + "seq="
                + seq
                + ", method='"
                + method
                + '\''
                + ", args="
                + Arrays.toString(args)
                + '}';
    }
}
