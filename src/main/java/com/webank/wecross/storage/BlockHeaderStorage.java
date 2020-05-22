package com.webank.wecross.storage;

import java.util.function.BiConsumer;

// storage for one stub
public interface BlockHeaderStorage {

    public long readBlockNumber();

    public byte[] readBlockHeader(long blockNumber);

    public void writeBlockHeader(long blockNumber, byte[] blockHeader);

    public void close();

    public void registerOnBlockHeader(BiConsumer<Long, byte[]> fn);
}
