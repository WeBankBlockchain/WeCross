package com.webank.wecross.config;

import com.webank.wecross.stubmanager.MemoryBlockManagerFactory;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResourceBlockManagerFactoryConfig {
    @Resource ResourceThreadPoolConfig.ResourceThreadPool resourceThreadPool;

    @Bean
    public MemoryBlockManagerFactory newResourceBlockManagerFactory() {
        return new MemoryBlockManagerFactory(resourceThreadPool);
    }
}
