package com.webank.wecross.stub;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Block {
    @JsonIgnore public byte[] rawBytes;
    public BlockHeader blockHeader;
    public List<String> transactionsHashes = new LinkedList<>();
    public List<Transaction> transactionsWithDetail = new LinkedList<>();

    public BlockHeader getBlockHeader() {
        return blockHeader;
    }

    public void setBlockHeader(BlockHeader blockHeader) {
        this.blockHeader = blockHeader;
    }

    public List<String> getTransactionsHashes() {
        return transactionsHashes;
    }

    public void setTransactionsHashes(List<String> transactionsHashes) {
        this.transactionsHashes = transactionsHashes;
    }

    public byte[] getRawBytes() {
        return rawBytes;
    }

    public void setRawBytes(byte[] rawBytes) {
        this.rawBytes = rawBytes;
    }

    public List<Transaction> getTransactionsWithDetail() {
        return transactionsWithDetail;
    }

    public void setTransactionsWithDetail(List<Transaction> transactionsWithDetail) {
        this.transactionsWithDetail = transactionsWithDetail;
    }

    @Override
    public String toString() {
        return "Block{"
                + "rawBytes="
                + Arrays.toString(rawBytes)
                + ", blockHeader="
                + blockHeader
                + ", transactionsHashes="
                + Arrays.toString(transactionsHashes.toArray())
                + ", transactionsWithDetail="
                + Arrays.toString(transactionsWithDetail.toArray())
                + '}';
    }
}
