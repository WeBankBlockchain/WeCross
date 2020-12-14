package com.webank.wecross.config;

import com.webank.wecross.account.UniversalAccountFactory;
import com.webank.wecross.stubmanager.StubManager;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UniversalAccountFactoryConfig {
    @Resource StubManager stubManager;

    @Bean
    public UniversalAccountFactory newUniversalAccountFactory() {
        UniversalAccountFactory universalAccountFactory = new UniversalAccountFactory();
        universalAccountFactory.setStubManager(stubManager);
        return universalAccountFactory;
    }
}
