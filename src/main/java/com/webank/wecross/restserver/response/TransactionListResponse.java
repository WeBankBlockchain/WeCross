package com.webank.wecross.restserver.response;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TransactionListResponse {
    private long nextBlockNumber;
    private int nextOffset;
    private List<Transaction> transactions = Collections.synchronizedList(new LinkedList<>());

    public TransactionListResponse() {}

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
}
