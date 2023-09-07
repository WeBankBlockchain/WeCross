package com.webank.wecross.stub;

public class BlockHeader {
    private long number;
    private String prevHash;
    private String hash;
    private String stateRoot;
    private String transactionRoot;
    private String receiptRoot;
    private long timestamp;

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public String getPrevHash() {
        return prevHash;
    }

    public void setPrevHash(String prevHash) {
        this.prevHash = prevHash;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getStateRoot() {
        return stateRoot;
    }

    public void setStateRoot(String stateRoot) {
        this.stateRoot = stateRoot;
    }

    public String getTransactionRoot() {
        return transactionRoot;
    }

    public void setTransactionRoot(String transactionRoot) {
        this.transactionRoot = transactionRoot;
    }

    public String getReceiptRoot() {
        return receiptRoot;
    }

    public void setReceiptRoot(String receiptRoot) {
        this.receiptRoot = receiptRoot;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "BlockHeader{"
                + "number="
                + number
                + ", prevHash='"
                + prevHash
                + '\''
                + ", hash='"
                + hash
                + '\''
                + ", stateRoot='"
                + stateRoot
                + '\''
                + ", transactionRoot='"
                + transactionRoot
                + '\''
                + ", receiptRoot='"
                + receiptRoot
                + '\''
                + ", timestamp='"
                + timestamp
                + '\''
                + '}';
    }
}
