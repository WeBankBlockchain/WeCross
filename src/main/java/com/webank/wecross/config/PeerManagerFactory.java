package com.webank.wecross.config;

import com.webank.wecross.p2p.HeartBeatProcessor;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.RequestProcessor;
import com.webank.wecross.p2p.ResponseProcessor;
import com.webank.wecross.p2p.netty.P2PService;
import com.webank.wecross.peer.PeerManager;
import com.webank.wecross.zone.ZoneManager;

import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PeerManagerFactory {
    @Resource
    private ZoneManager zoneManager;

    @Resource
    private P2PMessageEngine p2pEngine;

    @Resource
    private P2PService p2PService;
    
    @Resource
    HeartBeatProcessor heartBeatProcessor;
    @Resource
    ResponseProcessor resourceResponseProcessor;
    @Resource
    RequestProcessor resourceRequestProcessor;

    private long peerActiveTimeout = 17000; // 17s

    @Bean
    public PeerManager newPeerManager() {
        PeerManager peerManager = new PeerManager();
        peerManager.setZoneManager(zoneManager);
        peerManager.setPeerActiveTimeout(peerActiveTimeout);

        return peerManager;
    }
}
