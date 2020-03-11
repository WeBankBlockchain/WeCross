package com.webank.wecross.stub;

public class BlockHeader {
    private int number;
    private String prevHash;
    private String hash;
    private String stateRoot;
    private String transactionRoot;
    private String receiptRoot;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
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
}
