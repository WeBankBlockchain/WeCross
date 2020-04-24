package com.webank.wecross.config;

import com.moandjiezana.toml.Toml;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ThreadPoolTaskExecutorConfig {

    @Resource Toml toml;

    @Bean
    public ThreadPoolTaskExecutor newThreadPoolTaskExecutor() {
        System.out.println("Initializing ThreadPoolTaskExecutor ...");
        // init default thread pool

        final Long threadNum = toml.getLong("p2p.threadNum", 500L);
        ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();
        threadPool.setCorePoolSize(threadNum.intValue());
        threadPool.setMaxPoolSize(threadNum.intValue());
        threadPool.setQueueCapacity(1000);
        threadPool.setThreadNamePrefix("netty-p2p");
        threadPool.initialize();
        return threadPool;
    }
}
