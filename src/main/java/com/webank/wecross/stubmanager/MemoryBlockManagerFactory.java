package com.webank.wecross.stubmanager;

import com.webank.wecross.config.ResourceThreadPoolConfig;
import com.webank.wecross.zone.Chain;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;

public class MemoryBlockManagerFactory {
    private ResourceThreadPoolConfig.ResourceThreadPool resourceThreadPool;
    private Timer timer = new HashedWheelTimer();

    public MemoryBlockManagerFactory(
            ResourceThreadPoolConfig.ResourceThreadPool resourceThreadPool) {
        this.resourceThreadPool = resourceThreadPool;
    }

    public MemoryBlockManager build(Chain chain) {
        MemoryBlockManager resourceBlockManager = new MemoryBlockManager();
        resourceBlockManager.setThreadPool(resourceThreadPool.getThreadPool());
        resourceBlockManager.setChain(chain);
        resourceBlockManager.setTimer(timer);
        return resourceBlockManager;
    }
}
