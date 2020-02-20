package com.webank.wecross.config;

import com.webank.wecross.p2p.HeartBeatProcessor;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.ResourceRequestProcessor;
import com.webank.wecross.p2p.ResourceResponseProcessor;
import com.webank.wecross.p2p.netty.P2PService;
import com.webank.wecross.peer.PeerManager;
import com.webank.wecross.peer.SyncPeerMessageHandler;
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
    private SyncPeerMessageHandler messageHandler;

    @Resource
    private P2PService p2PService;
    
    @Resource
    HeartBeatProcessor heartBeatProcessor;
    @Resource
    ResourceResponseProcessor resourceResponseProcessor;
    @Resource
    ResourceRequestProcessor resourceRequestProcessor;

    private long peerActiveTimeout = 17000; // 17s

    @Bean
    public PeerManager newPeerManager() {
        PeerManager peerManager = new PeerManager();
        peerManager.setP2pEngine(p2pEngine);
        peerManager.setZoneManager(zoneManager);
        peerManager.setMessageHandler(messageHandler);
        peerManager.setPeerActiveTimeout(peerActiveTimeout);
        peerManager.setP2PService(p2PService);

        return peerManager;
    }

    public void setMessageHandler(SyncPeerMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }
}
