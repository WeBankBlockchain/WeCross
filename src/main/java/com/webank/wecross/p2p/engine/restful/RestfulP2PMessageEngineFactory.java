package com.webank.wecross.p2p.engine.restful;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class RestfulP2PMessageEngineFactory {
    private static Logger logger = LoggerFactory.getLogger(RestfulP2PMessageEngineFactory.class);
    private static ThreadPoolTaskExecutor threadPool;
    private static RestfulP2PMessageEngine engineInstance;

    public static RestfulP2PMessageEngine getEngineInstance() {
        // Init thread pool
        if (threadPool == null) {
            final int threadNum = 8;
            threadPool = new ThreadPoolTaskExecutor();
            threadPool.setCorePoolSize(threadNum);
            threadPool.setMaxPoolSize(threadNum);
            threadPool.setQueueCapacity(1000);
            threadPool.initialize();
        }

        // Init engine
        if (engineInstance == null) {
            logger.info("New RestfulP2PMessageEngine");
            engineInstance = new RestfulP2PMessageEngine();
            engineInstance.setThreadPool(threadPool);
        }

        return engineInstance;
    }
}
