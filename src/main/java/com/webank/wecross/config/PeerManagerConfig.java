package com.webank.wecross.config;

import com.webank.wecross.peer.PeerManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PeerManagerConfig {
    private long peerActiveTimeout = 17000; // 17s

    @Bean
    public PeerManager newPeerManager() {
        System.out.println(
                "Initializing newPeerManager with peerActiveTimeout("
                        + peerActiveTimeout
                        + ") ...");

        PeerManager peerManager = new PeerManager();
        peerManager.setPeerActiveTimeout(peerActiveTimeout);

        return peerManager;
    }
}
