package com.webank.wecross.stubmanager;

public interface BlockHeaderManager {
    public interface BlockHeaderCallback {
        void onBlockHeader(byte[] blockHeader);
    }

    public long getBlockNumber();

    public byte[] getBlockHeader(long blockNumber);

    void asyncGetBlockHeader(long blockNumber, BlockHeaderManager.BlockHeaderCallback callback);
}
