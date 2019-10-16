package com.webank.wecross.host;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PeerManagerFactory {
    @Bean
    public PeerManager newPeerManager() {
        return new PeerManager();
    }
}
