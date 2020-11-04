package com.webank.wecross.restserver.response;

import java.util.Arrays;

public class TransactionListResponse {
    private long nextBlockNumber;
    private int nextOffset;
    private Transaction[] transactions;

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

    public Transaction[] getTransactions() {
        return transactions;
    }

    public void setTransactions(Transaction[] transactions) {
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
                + Arrays.toString(transactions)
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
            return "TxInfo{" + "txHash='" + txHash + '\'' + ", blockNumber=" + blockNumber + '}';
        }
    }
}
