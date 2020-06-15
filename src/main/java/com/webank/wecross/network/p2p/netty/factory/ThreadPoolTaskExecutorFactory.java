package com.webank.wecross.network.p2p.netty.factory;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class ThreadPoolTaskExecutorFactory {

    public static ThreadPoolTaskExecutor build(Long threadNum, Long queueCapacity, String name) {
        System.out.println("Initializing ThreadPoolTaskExecutor ...");
        // init default thread pool

        ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();
        threadPool.setCorePoolSize(threadNum.intValue());
        threadPool.setMaxPoolSize(threadNum.intValue());
        threadPool.setQueueCapacity(queueCapacity.intValue());
        threadPool.setThreadNamePrefix(name);
        threadPool.initialize();
        return threadPool;
    }
}
