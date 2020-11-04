package com.webank.wecross.stub;

import java.util.Arrays;

public class Transaction {
    private byte[] txBytes; // raw transaction info
    private byte[] receiptBytes; // raw transaction receipt info

    private String accountIdentity; // sender's chain account identity

    private String xaTransactionID;
    private long xaTransactionSeq;

    private String resource;

    private long blockNumber;
    private String txHash;
    private TransactionRequest transactionRequest;
    private TransactionResponse transactionResponse;

    private boolean transactionByProxy = false;

    public Transaction(
            long blockNumber,
            String txHash,
            TransactionRequest transactionRequest,
            TransactionResponse transactionResponse) {
        this.blockNumber = blockNumber;
        this.txHash = txHash;
        this.transactionRequest = transactionRequest;
        this.transactionResponse = transactionResponse;
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

    public String getAccountIdentity() {
        return accountIdentity;
    }

    public void setAccountIdentity(String accountIdentity) {
        this.accountIdentity = accountIdentity;
    }

    public boolean isTransactionByProxy() {
        return transactionByProxy;
    }

    public void setTransactionByProxy(boolean transactionByProxy) {
        this.transactionByProxy = transactionByProxy;
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

    @Override
    public String toString() {
        return "Transaction{"
                + "txBytes="
                + Arrays.toString(txBytes)
                + ", receiptBytes="
                + Arrays.toString(receiptBytes)
                + ", accountIdentity='"
                + accountIdentity
                + '\''
                + ", xaTransactionID='"
                + xaTransactionID
                + '\''
                + ", xaTransactionSeq="
                + xaTransactionSeq
                + ", resource='"
                + resource
                + '\''
                + ", blockNumber="
                + blockNumber
                + ", transactionHash='"
                + txHash
                + '\''
                + ", transactionRequest="
                + transactionRequest
                + ", transactionResponse="
                + transactionResponse
                + ", transactionByProxy="
                + transactionByProxy
                + '}';
    }
}
