package com.webank.wecross.resource;

import com.webank.wecross.config.ResourceThreadPoolConfig;
import com.webank.wecross.zone.Chain;

public class ResourceBlockHeaderManagerFactory {
    private ResourceThreadPoolConfig.ResourceThreadPool resourceThreadPool;

    public ResourceBlockHeaderManagerFactory(
            ResourceThreadPoolConfig.ResourceThreadPool resourceThreadPool) {
        this.resourceThreadPool = resourceThreadPool;
    }

    public ResourceBlockHeaderManager build(Chain chain) {
        ResourceBlockHeaderManager resourceBlockHeaderManager = new ResourceBlockHeaderManager();
        resourceBlockHeaderManager.setBlockHeaderStorage(chain.getBlockHeaderStorage());
        resourceBlockHeaderManager.setChain(chain);
        resourceBlockHeaderManager.setThreadPool(resourceThreadPool.getThreadPool());
        return resourceBlockHeaderManager;
    }
}
