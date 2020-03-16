package com.webank.wecross.storage;

// storage for one stub
public interface BlockHeaderStorage {
    public int readBlockNumber();

    public byte[] readBlockHeader(int blockNumber);

    public void writeBlockHeader(int blockNumber, byte[] blockHeader);
}
