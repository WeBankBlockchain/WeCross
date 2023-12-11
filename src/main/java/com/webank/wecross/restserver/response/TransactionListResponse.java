package com.webank.wecross.restserver.response;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TransactionListResponse {
    private long nextBlockNumber;
    private int nextOffset;
    private List<Transaction> transactions = Collections.synchronizedList(new LinkedList<>());

    private List<TransactionWithDetail> transactionWithDetails =
            Collections.synchronizedList(new LinkedList<>());

    public TransactionListResponse() {}

    public void addTransactionWithDetail(TransactionWithDetail transactionWithDetail) {
        this.transactionWithDetails.add(transactionWithDetail);
    }

    public void addTransactionWithDetails(List<TransactionWithDetail> transactionWithDetails) {
        this.transactionWithDetails.addAll(transactionWithDetails);
    }

    public void addTransaction(Transaction transaction) {
        this.transactions.add(transaction);
    }

    public void addTransactions(List<Transaction> transactions) {
        this.transactions.addAll(transactions);
    }

    public long getNextBlockNumber() {
        return nextBlockNumber;
    }

    public void setNextBlockNumber(long nextBlockNumber) {
        this.nextBlockNumber = nextBlockNumber;
    }

    public int getNextOffset() {
        return nextOffset;
    }

    public void setNextOffset(int nextOffset) {
        this.nextOffset = nextOffset;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public List<TransactionWithDetail> getTransactionWithDetails() {
        return transactionWithDetails;
    }

    public void setTransactionWithDetails(List<TransactionWithDetail> transactionWithDetails) {
        this.transactionWithDetails = transactionWithDetails;
    }

    @Override
    public String toString() {
        return "TransactionListResponse{"
                + "nextBlockNumber="
                + nextBlockNumber
                + ", nextOffset="
                + nextOffset
                + ", transactions="
                + Arrays.toString(transactions.toArray())
                + '}';
    }

    public static class Transaction {
        private String txHash;
        private long blockNumber;

        public String getTxHash() {
            return txHash;
        }

        public void setTxHash(String txHash) {
            this.txHash = txHash;
        }

        public long getBlockNumber() {
            return blockNumber;
        }

        public void setBlockNumber(long blockNumber) {
            this.blockNumber = blockNumber;
        }

        @Override
        public String toString() {
            return "Transaction{"
                    + "txHash='"
                    + txHash
                    + '\''
                    + ", blockNumber="
                    + blockNumber
                    + '}';
        }
    }

    public static class TransactionWithDetail {
        private String txHash;
        private long blockNumber;
        private String accountIdentity;
        private String path;
        private String method;
        private String xaTransactionID;

        public String getTxHash() {
            return txHash;
        }

        public void setTxHash(String txHash) {
            this.txHash = txHash;
        }

        public long getBlockNumber() {
            return blockNumber;
        }

        public void setBlockNumber(long blockNumber) {
            this.blockNumber = blockNumber;
        }

        public String getAccountIdentity() {
            return accountIdentity;
        }

        public void setAccountIdentity(String accountIdentity) {
            this.accountIdentity = accountIdentity;
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

        public String getXaTransactionID() {
            return xaTransactionID;
        }

        public void setXaTransactionID(String xaTransactionID) {
            this.xaTransactionID = xaTransactionID;
        }

        @Override
        public String toString() {
            return "TransactionWithDetail{"
                    + "txHash='"
                    + txHash
                    + '\''
                    + ", blockNumber="
                    + blockNumber
                    + ", accountIdentity='"
                    + accountIdentity
                    + '\''
                    + ", path='"
                    + path
                    + '\''
                    + ", method='"
                    + method
                    + '\''
                    + ", xaTransactionID='"
                    + xaTransactionID
                    + '\''
                    + '}';
        }
    }
}
