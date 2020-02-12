package com.webank.wecross.config;

import com.webank.wecross.p2p.P2PMessageEngine;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class P2PMessageEngineFactory {

    @Resource(name = "newRestfulP2PMessageEngine")
    private P2PMessageEngine restfulEngineInstance;

    // private static NettyP2PMessageEngine nettyEngine = facXXX.getXXX();

    @Bean
    public P2PMessageEngine newP2PMessageEngine() {
        return restfulEngineInstance;
    }
}
