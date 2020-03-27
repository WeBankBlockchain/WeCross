package com.webank.wecross.storage;

import com.webank.wecross.stub.BlockHeaderManager;

public class RocksDBBlockHeaderManager implements BlockHeaderManager {
    private BlockHeaderStorage blockHeaderStorage;

    @Override
    public long getBlockNumber() {
        return blockHeaderStorage.readBlockNumber();
    }

    @Override
    public byte[] getBlockHeader(long blockNumber) {
        return blockHeaderStorage.readBlockHeader(blockNumber);
    }
}
