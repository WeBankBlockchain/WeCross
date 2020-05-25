package com.webank.wecross.resource;

import com.webank.wecross.config.ResourceThreadPoolConfig;
import com.webank.wecross.storage.BlockHeaderStorage;

public class ResourceBlockHeaderManagerFactory {
    private ResourceThreadPoolConfig.ResourceThreadPool resourceThreadPool;

    public ResourceBlockHeaderManagerFactory(
            ResourceThreadPoolConfig.ResourceThreadPool resourceThreadPool) {
        this.resourceThreadPool = resourceThreadPool;
    }

    public ResourceBlockHeaderManager build(BlockHeaderStorage blockHeaderStorage) {
        ResourceBlockHeaderManager resourceBlockHeaderManager = new ResourceBlockHeaderManager();
        resourceBlockHeaderManager.setBlockHeaderStorage(blockHeaderStorage);
        resourceBlockHeaderManager.setThreadPool(resourceThreadPool.getThreadPool());
        return resourceBlockHeaderManager;
    }
}
