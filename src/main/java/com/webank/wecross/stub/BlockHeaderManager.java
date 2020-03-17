package com.webank.wecross.stub;

public interface BlockHeaderManager {
    public long getBlockNumber();

    public byte[] getBlock(long blockNumber);
}
