package com.webank.wecross.stub;

public class Transaction {

    private byte[] txBytes; // raw transaction info
    private byte[] receiptBytes; // raw transaction receipt info

    private String transactionID;
    private String seq;
    private String resource;

    private String sender; // identity of transaction sender

    private long blockNumber;
    private String transactionHash;
    private TransactionRequest transactionRequest;
    private TransactionResponse transactionResponse;

    private boolean transactionByProxy = false;

    public Transaction(
            long blockNumber,
            String transactionHash,
            TransactionRequest transactionRequest,
            TransactionResponse transactionResponse) {
        this.blockNumber = blockNumber;
        this.transactionHash = transactionHash;
        this.transactionRequest = transactionRequest;
        this.transactionResponse = transactionResponse;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public TransactionRequest getTransactionRequest() {
        return transactionRequest;
    }

    public void setTransactionRequest(TransactionRequest transactionRequest) {
        this.transactionRequest = transactionRequest;
    }

    public TransactionResponse getTransactionResponse() {
        return transactionResponse;
    }

    public void setTransactionResponse(TransactionResponse transactionResponse) {
        this.transactionResponse = transactionResponse;
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

    public String getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public boolean isTransactionByProxy() {
        return transactionByProxy;
    }

    public void setTransactionByProxy(boolean transactionByProxy) {
        this.transactionByProxy = transactionByProxy;
    }

    public String getSeq() {
        return seq;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }

    @Override
    public String toString() {
        return "Transaction{"
                + "transactionID='"
                + transactionID
                + '\''
                + ", seq="
                + seq
                + ", resource="
                + resource
                + ", blockNumber="
                + blockNumber
                + ", transactionHash='"
                + transactionHash
                + '\''
                + ", transactionByProxy="
                + transactionByProxy
                + ", transactionRequest="
                + transactionRequest
                + ", transactionResponse="
                + transactionResponse
                + '}';
    }
}
