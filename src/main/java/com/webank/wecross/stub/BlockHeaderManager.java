package com.webank.wecross.stub;

public interface BlockHeaderManager {
    public void start();

    public void stop();

    public interface GetBlockNumberCallback {
        void onResponse(Exception e, long blockNumber);
    }

    public void asyncGetBlockNumber(GetBlockNumberCallback callback);

    public interface GetBlockHeaderCallback {
        void onResponse(Exception e, byte[] blockHeader);
    }

    void asyncGetBlockHeader(long blockNumber, BlockHeaderManager.GetBlockHeaderCallback callback);
}
