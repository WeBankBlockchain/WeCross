package com.webank.wecross.config;

import com.webank.wecross.stubmanager.MemoryBlockHeaderManagerFactory;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResourceBlockHeaderManagerFactoryConfig {
    @Resource ResourceThreadPoolConfig.ResourceThreadPool resourceThreadPool;

    @Bean
    public MemoryBlockHeaderManagerFactory newResourceBlockHeaderManagerFactory() {
        return new MemoryBlockHeaderManagerFactory(resourceThreadPool);
    }
}
