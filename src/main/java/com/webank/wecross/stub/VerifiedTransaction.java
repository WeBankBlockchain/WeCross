package com.webank.wecross.stub;

public class VerifiedTransaction {
    private long blockNumber;
    private String transactionHash;
    private String realAddress;
    private TransactionRequest transactionRequest;
    private TransactionResponse transactionResponse;

    public VerifiedTransaction(
            long blockNumber,
            String transactionHash,
            String realAddress,
            TransactionRequest transactionRequest,
            TransactionResponse transactionResponse) {
        this.blockNumber = blockNumber;
        this.transactionHash = transactionHash;
        this.realAddress = realAddress;
        this.transactionRequest = transactionRequest;
        this.transactionResponse = transactionResponse;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public String getRealAddress() {
        return realAddress;
    }

    public TransactionRequest getTransactionRequest() {
        return transactionRequest;
    }

    public TransactionResponse getTransactionResponse() {
        return transactionResponse;
    }
}
