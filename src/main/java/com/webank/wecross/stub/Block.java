package com.webank.wecross.stub;

import java.util.LinkedList;
import java.util.List;

public class Block {
    public byte[] rawBytes;
    public BlockHeader blockHeader;
    public List<String> transactionsHashes = new LinkedList<>();

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
}
