package com.webank.wecross.p2p;

import com.webank.wecross.p2p.engine.p2p.RestfulP2PMessageEngine;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class P2PMessageEngineFactory {
    @Resource(name = "newRestfulP2PMessageEngine")
    private RestfulP2PMessageEngine restfulEngine;

    @Bean
    public P2PMessageEngine newP2PMessageEngine() {
        return restfulEngine;
    }

    public void setRestfulEngine(RestfulP2PMessageEngine restfulEngine) {
        this.restfulEngine = restfulEngine;
    }
}
