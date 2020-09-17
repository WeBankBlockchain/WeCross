package com.webank.wecross.stub;

public interface BlockManager {
    public void start();

    public void stop();

    public interface GetBlockNumberCallback {
        void onResponse(Exception e, long blockNumber);
    }

    public void asyncGetBlockNumber(GetBlockNumberCallback callback);

    public interface GetBlockCallback {
        void onResponse(Exception e, Block block);
    }

    void asyncGetBlock(long blockNumber, GetBlockCallback callback);
}
