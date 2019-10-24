package com.webank.wecross.p2p.config;

import com.webank.wecross.p2p.engine.restful.RestfulP2PMessageEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class RestfulP2PMessageEngineFactory {

    @Bean
    public RestfulP2PMessageEngine newRestfulP2PMessageEngine() {
        Logger logger = LoggerFactory.getLogger(RestfulP2PMessageEngineFactory.class);

        // Init thread pool
        final int threadNum = 8;
        ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();
        threadPool.setCorePoolSize(threadNum);
        threadPool.setMaxPoolSize(threadNum);
        threadPool.setQueueCapacity(1000);
        threadPool.initialize();

        // Init engine
        logger.info("New RestfulP2PMessageEngine");
        RestfulP2PMessageEngine engineInstance = new RestfulP2PMessageEngine();
        engineInstance.setThreadPool(threadPool);

        return engineInstance;
    }
}
