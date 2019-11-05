package com.webank.wecross.p2p.config;

import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.engine.RestfulP2PMessageEngine;
import com.webank.wecross.p2p.netty.P2PService;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestfulP2PMessageEngineFactory {

    private Logger logger = LoggerFactory.getLogger(RestfulP2PMessageEngineFactory.class);

    @Resource(name = "newP2PService")
    P2PService p2PService;

    @Bean
    public P2PMessageEngine newRestfulP2PMessageEngine() {

        // Init engine
        logger.info("New RestfulP2PMessageEngine");
        RestfulP2PMessageEngine engineInstance = new RestfulP2PMessageEngine();
        engineInstance.setP2PService(p2PService);

        return engineInstance;
    }
}
