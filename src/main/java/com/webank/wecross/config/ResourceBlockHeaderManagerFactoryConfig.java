package com.webank.wecross.config;

import com.webank.wecross.resource.ResourceBlockHeaderManagerFactory;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResourceBlockHeaderManagerFactoryConfig {
    @Resource ResourceThreadPoolConfig.ResourceThreadPool resourceThreadPool;

    @Bean
    public ResourceBlockHeaderManagerFactory newResourceBlockHeaderManagerFactory() {
        return new ResourceBlockHeaderManagerFactory(resourceThreadPool);
    }
}
