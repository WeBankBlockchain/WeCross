package com.webank.wecross.config;

import com.moandjiezana.toml.Toml;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ResourceThreadPoolConfig {
    private static Logger logger = LoggerFactory.getLogger(ResourceThreadPoolConfig.class);

    @Resource Toml toml;

    public static class ResourceThreadPool {
        private ThreadPoolTaskExecutor threadPool;

        public ResourceThreadPool(int corePoolSize, int maxPoolSize, int queueCapacity) {
            this.threadPool = new ThreadPoolTaskExecutor();
            this.threadPool.setCorePoolSize(corePoolSize);
            this.threadPool.setMaxPoolSize(maxPoolSize);
            this.threadPool.setQueueCapacity(queueCapacity);
            this.threadPool.setThreadNamePrefix("resource");
            this.threadPool.initialize();
        }

        public ThreadPoolTaskExecutor getThreadPool() {
            return threadPool;
        }
    }

    @Bean
    public ResourceThreadPool newResourceThreadPool() {
        Long corePoolSize = toml.getLong("advanced.resourceThreadPool.corePoolSize");
        if (corePoolSize == null) {
            corePoolSize = new Long(8);
            logger.debug(
                    "[advanced.resourceThreadPool.corePoolSize] not set, use default: "
                            + corePoolSize);
        }

        Long maxPoolSize = toml.getLong("advanced.resourceThreadPool.maxPoolSize");
        if (maxPoolSize == null) {
            maxPoolSize = new Long(8);
            logger.debug(
                    "[advanced.resourceThreadPool.maxPoolSize] not set, use default: "
                            + maxPoolSize);
        }

        Long queueCapacity = toml.getLong("advanced.resourceThreadPool.threadQueueCapacity");
        if (queueCapacity == null) {
            queueCapacity = new Long(10000);
            logger.debug(
                    "[advanced.resourceThreadPool.threadQueueCapacity] not set, use default: "
                            + queueCapacity);
        }

        return new ResourceThreadPool(
                corePoolSize.intValue(), maxPoolSize.intValue(), queueCapacity.intValue());
    }
}
