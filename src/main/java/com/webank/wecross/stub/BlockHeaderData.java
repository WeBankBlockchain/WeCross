package com.webank.wecross.stub;

public class BlockHeaderData {
    private BlockHeader blockHeader;
    private byte[] data;

    public BlockHeaderData(BlockHeader blockHeader, byte[] data) {
        this.blockHeader = blockHeader;
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public BlockHeader getBlockHeader() {
        return blockHeader;
    }

    public void setBlockHeader(BlockHeader blockHeader) {
        this.blockHeader = blockHeader;
    }
}
