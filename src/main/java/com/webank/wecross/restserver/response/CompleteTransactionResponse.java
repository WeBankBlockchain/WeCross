package com.webank.wecross.restserver.response;

import java.util.Arrays;

public class CompleteTransactionResponse {
    private String path;
    private String username;
    private long blockNumber;
    private String txHash;

    private String xaTransactionID;
    private long xaTransactionSeq;

    private String method;
    private String[] args;
    private String[] result;

    private boolean byProxy = false;

    private byte[] txBytes; // raw transaction info
    private byte[] receiptBytes; // raw transaction receipt info

    private int errorCode; // transaction rolled back
    private String message;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public String getXaTransactionID() {
        return xaTransactionID;
    }

    public void setXaTransactionID(String xaTransactionID) {
        this.xaTransactionID = xaTransactionID;
    }

    public long getXaTransactionSeq() {
        return xaTransactionSeq;
    }

    public void setXaTransactionSeq(long xaTransactionSeq) {
        this.xaTransactionSeq = xaTransactionSeq;
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

    public String[] getResult() {
        return result;
    }

    public void setResult(String[] result) {
        this.result = result;
    }

    public boolean isByProxy() {
        return byProxy;
    }

    public void setByProxy(boolean byProxy) {
        this.byProxy = byProxy;
    }

    public byte[] getTxBytes() {
        return txBytes;
    }

    public void setTxBytes(byte[] txBytes) {
        this.txBytes = txBytes;
    }

    public byte[] getReceiptBytes() {
        return receiptBytes;
    }

    public void setReceiptBytes(byte[] receiptBytes) {
        this.receiptBytes = receiptBytes;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "CompleteTransactionResponse{"
                + "path='"
                + path
                + '\''
                + ", username='"
                + username
                + '\''
                + ", blockNumber="
                + blockNumber
                + ", txHash='"
                + txHash
                + '\''
                + ", xaTransactionID='"
                + xaTransactionID
                + '\''
                + ", xaTransactionSeq="
                + xaTransactionSeq
                + ", method='"
                + method
                + '\''
                + ", args="
                + Arrays.toString(args)
                + ", result="
                + Arrays.toString(result)
                + ", byProxy="
                + byProxy
                + ", txBytes="
                + Arrays.toString(txBytes)
                + ", receiptBytes="
                + Arrays.toString(receiptBytes)
                + ", errorCode="
                + errorCode
                + ", message='"
                + message
                + '\''
                + '}';
    }
}
