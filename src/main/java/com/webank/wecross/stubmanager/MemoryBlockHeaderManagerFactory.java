package com.webank.wecross.stubmanager;

import com.webank.wecross.config.ResourceThreadPoolConfig;
import com.webank.wecross.zone.Chain;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;

public class MemoryBlockHeaderManagerFactory {
    private ResourceThreadPoolConfig.ResourceThreadPool resourceThreadPool;
    private Timer timer = new HashedWheelTimer();

    public MemoryBlockHeaderManagerFactory(
            ResourceThreadPoolConfig.ResourceThreadPool resourceThreadPool) {
        this.resourceThreadPool = resourceThreadPool;
    }

    public MemoryBlockHeaderManager build(Chain chain) {
        MemoryBlockHeaderManager resourceBlockHeaderManager = new MemoryBlockHeaderManager();
        resourceBlockHeaderManager.setThreadPool(resourceThreadPool.getThreadPool());
        resourceBlockHeaderManager.setChain(chain);
        resourceBlockHeaderManager.setTimer(timer);
        return resourceBlockHeaderManager;
    }
}
