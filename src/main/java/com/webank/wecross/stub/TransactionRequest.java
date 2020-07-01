package com.webank.wecross.stub;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TransactionRequest {
    private String path;
    private String method;
    private String[] args;

    // Optional args
    // transactionID, peers, etc...
    private Map<String, Object> options = new HashMap<String, Object>();

    public TransactionRequest() {}

    public TransactionRequest(String method, String[] args) {
        this.method = method;
        this.args = args;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }

    @Override
    public String toString() {
        return "TransactionRequest{"
                + "path='"
                + path
                + '\''
                + ", method='"
                + method
                + '\''
                + ", args="
                + Arrays.toString(args)
                + ", options="
                + options
                + '}';
    }
}
