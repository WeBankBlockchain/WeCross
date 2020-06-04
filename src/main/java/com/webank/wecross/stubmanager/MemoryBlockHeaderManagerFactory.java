package com.webank.wecross.stubmanager;

import com.webank.wecross.config.ResourceThreadPoolConfig;
import com.webank.wecross.zone.Chain;

public class MemoryBlockHeaderManagerFactory {
    private ResourceThreadPoolConfig.ResourceThreadPool resourceThreadPool;

    public MemoryBlockHeaderManagerFactory(
            ResourceThreadPoolConfig.ResourceThreadPool resourceThreadPool) {
        this.resourceThreadPool = resourceThreadPool;
    }

    public MemoryBlockHeaderManager build(Chain chain) {
        MemoryBlockHeaderManager resourceBlockHeaderManager = new MemoryBlockHeaderManager();
        resourceBlockHeaderManager.setThreadPool(resourceThreadPool.getThreadPool());
        return resourceBlockHeaderManager;
    }
}
