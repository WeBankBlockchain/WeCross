package com.webank.wecross.resource;

import com.webank.wecross.storage.BlockHeaderStorage;
import com.webank.wecross.stub.BlockHeaderManager;

public class ResourceBlockHeaderManager implements BlockHeaderManager {
    private BlockHeaderStorage blockHeaderStorage;

    @Override
    public long getBlockNumber() {
        return blockHeaderStorage.readBlockNumber();
    }

    @Override
    public byte[] getBlockHeader(long blockNumber) {
        byte[] data = null;
        while (data == null) {
            data = blockHeaderStorage.readBlockHeader(blockNumber);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return null;
            }
        }

        return data;
    }

    public BlockHeaderStorage getBlockHeaderStorage() {
        return blockHeaderStorage;
    }

    public void setBlockHeaderStorage(BlockHeaderStorage blockHeaderStorage) {
        this.blockHeaderStorage = blockHeaderStorage;
    }
}
