package com.webank.wecross.config;

import com.webank.wecross.restserver.RPCContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RPCContextConfig {
    @Bean
    public RPCContext newRestContext() {
        return new RPCContext();
    }
}
