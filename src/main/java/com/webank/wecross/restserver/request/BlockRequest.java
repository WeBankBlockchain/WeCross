package com.webank.wecross.restserver.request;

public class BlockRequest {
    private long blockNumber;
    private String path;

    public BlockRequest() {}

    public BlockRequest(long blockNumber, String path) {
        this.blockNumber = blockNumber;
        this.path = path;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
