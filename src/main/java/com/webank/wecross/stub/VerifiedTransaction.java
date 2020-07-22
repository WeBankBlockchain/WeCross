package com.webank.wecross.stub;

public class VerifiedTransaction {
    private long blockNumber;
    private String transactionHash;
    private Path path;
    private String realAddress;
    private TransactionRequest transactionRequest;
    private TransactionResponse transactionResponse;

    public VerifiedTransaction(
            long blockNumber,
            String transactionHash,
            Path path,
            String realAddress,
            TransactionRequest transactionRequest,
            TransactionResponse transactionResponse) {
        this.blockNumber = blockNumber;
        this.transactionHash = transactionHash;
        this.path = path;
        this.realAddress = realAddress;
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

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public String getRealAddress() {
        return realAddress;
    }

    public void setRealAddress(String realAddress) {
        this.realAddress = realAddress;
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

    @Override
    public String toString() {
        return "VerifiedTransaction{"
                + "blockNumber="
                + blockNumber
                + ", transactionHash='"
                + transactionHash
                + '\''
                + ", path="
                + path
                + ", realAddress='"
                + realAddress
                + '\''
                + ", transactionRequest="
                + transactionRequest
                + ", transactionResponse="
                + transactionResponse
                + '}';
    }
}
