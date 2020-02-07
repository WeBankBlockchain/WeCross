package com.webank.wecross.peer;

import com.webank.wecross.stub.BlockHeader;

public class StubSyncManager {
    public int getBlockNumber(String path) {
        return 0;
    }

    public BlockHeader getBlockHeader(String path, int blockNumber) {
        return null;
    }

    public void start() {}
}
