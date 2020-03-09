package com.webank.wecross.storage;

import com.webank.wecross.chain.BlockHeader;

// storage for one stub
public interface BlockHeaderStorage {
    public int readBlockNumber();

    public BlockHeader readBlockHeader(int blockNumber);

    public void writeBlockHeader(int blockNumber, BlockHeader blockHeader);
}
