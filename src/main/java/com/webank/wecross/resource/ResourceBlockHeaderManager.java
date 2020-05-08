package com.webank.wecross.resource;

import com.webank.wecross.storage.BlockHeaderStorage;
import com.webank.wecross.stub.BlockHeaderManager;
import com.webank.wecross.zone.Chain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceBlockHeaderManager implements BlockHeaderManager {
    private Logger logger = LoggerFactory.getLogger(ResourceBlockHeaderManager.class);
    private BlockHeaderStorage blockHeaderStorage;
    private Chain chain;

    @Override
    public long getBlockNumber() {
        return blockHeaderStorage.readBlockNumber();
    }

    @Override
    public byte[] getBlockHeader(long blockNumber) {
        byte[] data = null;
        chain.fetchBlockHeaderByNumber(blockNumber);

        while (data == null) {
            data = blockHeaderStorage.readBlockHeader(blockNumber);
            if (data != null) {
                break;
            }

            try {
                Thread.sleep(100);
            } catch (Exception e) {
                logger.warn("Thread exception", e);
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

    public Chain getChain() {
        return chain;
    }

    public void setChain(Chain chain) {
        this.chain = chain;
    }
}
