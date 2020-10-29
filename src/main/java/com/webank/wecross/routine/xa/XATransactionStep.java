package com.webank.wecross.routine.xa;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class XATransactionStep {
    private long xaTransactionSeq;
    private String accountIdentity;
    private String username;
    private String path;
    private long timestamp;
    private String method;
    private String args;

    public long getXaTransactionSeq() {
        return xaTransactionSeq;
    }

    public void setXaTransactionSeq(long xaTransactionSeq) {
        this.xaTransactionSeq = xaTransactionSeq;
    }

    public String getAccountIdentity() {
        return accountIdentity;
    }

    public void setAccountIdentity(String accountIdentity) {
        this.accountIdentity = accountIdentity;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    @Override
    public String toString() {
        return "XATransactionStep{"
                + "xaTransactionSeq="
                + xaTransactionSeq
                + ", accountIdentity='"
                + accountIdentity
                + '\''
                + ", username='"
                + username
                + '\''
                + ", path='"
                + path
                + '\''
                + ", timestamp="
                + timestamp
                + ", method='"
                + method
                + '\''
                + ", args='"
                + args
                + '\''
                + '}';
    }
}
