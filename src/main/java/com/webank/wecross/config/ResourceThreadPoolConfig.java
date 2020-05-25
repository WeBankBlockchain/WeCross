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
            this.threadPool.initialize();
        }

        public ThreadPoolTaskExecutor getThreadPool() {
            return threadPool;
        }
    }

    @Bean
    public ResourceThreadPool newResourceThreadPool() {
        Long corePoolSize = toml.getLong("advance.resourceThreadPool.corePoolSize");
        if (corePoolSize == null) {
            logger.debug("[advance.resourceThreadPool.corePoolSize] not set, use default: " + 200);
            corePoolSize = new Long(200);
        }

        Long maxPoolSize = toml.getLong("advance.resourceThreadPool.maxPoolSize");
        if (maxPoolSize == null) {
            logger.debug("[advance.resourceThreadPool.maxPoolSize] not set, use default: " + 200);
            maxPoolSize = new Long(200);
        }

        Long queueCapacity = toml.getLong("advance.resourceThreadPool.queueCapacity");
        if (queueCapacity == null) {
            logger.debug(
                    "[advance.resourceThreadPool.queueCapacity] not set, use default: " + 5000);
            queueCapacity = new Long(5000);
        }

        return new ResourceThreadPool(
                corePoolSize.intValue(), maxPoolSize.intValue(), queueCapacity.intValue());
    }
}
