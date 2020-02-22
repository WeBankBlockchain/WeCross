package com.webank.wecross.config;

import com.webank.wecross.peer.PeerManager;
import com.webank.wecross.zone.ZoneManager;

import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PeerManagerConfig {
    @Resource
    private ZoneManager zoneManager;

    private long peerActiveTimeout = 17000; // 17s

    @Bean
    public PeerManager newPeerManager() {
        PeerManager peerManager = new PeerManager();
        peerManager.setZoneManager(zoneManager);
        peerManager.setPeerActiveTimeout(peerActiveTimeout);

        return peerManager;
    }
}
