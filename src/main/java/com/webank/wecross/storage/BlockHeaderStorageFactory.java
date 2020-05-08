package com.webank.wecross.storage;

public interface BlockHeaderStorageFactory {
    public BlockHeaderStorage newBlockHeaderStorage(String path);
}
