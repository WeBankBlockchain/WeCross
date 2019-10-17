package com.webank.wecross.restserver.p2p;

import com.webank.wecross.p2p.P2PMessageEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestfulP2PMessageEngineFactory {
    private Logger logger = LoggerFactory.getLogger(RestfulP2PMessageEngineFactory.class);

    @Bean
    public P2PMessageEngine newRestfulP2PMessageEngine() {
        logger.info("New  RestfulP2PMessageEngine");
        return new RestfulP2PMessageEngine();
    }
}
