package com.webank.wecross.stub;

import java.util.Arrays;

public class Transaction {
    private byte[] txBytes; // raw transaction info
    private byte[] receiptBytes; // raw transaction receipt info

    private String accountIdentity; // sender's chain account identity
    private String resource;

    private TransactionRequest transactionRequest = new TransactionRequest();
    private TransactionResponse transactionResponse = new TransactionResponse();

    private boolean transactionByProxy = false;

    public Transaction() {}

    public Transaction(
            TransactionRequest transactionRequest, TransactionResponse transactionResponse) {
        this.transactionRequest = transactionRequest;
        this.transactionResponse = transactionResponse;
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
                + ", resource='"
                + resource
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
