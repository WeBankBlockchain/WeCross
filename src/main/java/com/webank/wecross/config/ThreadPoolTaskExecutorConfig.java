package com.webank.wecross.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ThreadPoolTaskExecutorConfig {

    @Bean
    public ThreadPoolTaskExecutor newThreadPoolTaskExecutor() {
        System.out.println("Initializing ThreadPoolTaskExecutor ...");
        // init default thread pool

        final int threadNum = 8;
        ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();
        threadPool.setCorePoolSize(threadNum);
        threadPool.setMaxPoolSize(threadNum);
        threadPool.setQueueCapacity(1000);
        threadPool.setThreadNamePrefix("netty-p2p");
        threadPool.initialize();
        return threadPool;
    }
}
