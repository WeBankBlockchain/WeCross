package com.webank.wecross.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.netty.P2PService;
import com.webank.wecross.peer.PeerManager;
import com.webank.wecross.zone.ZoneManager;

import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WeCrossHostConfig {

    @Resource
    ZoneManager zoneManager;
    
    @Resource
    P2PService p2pService;
    
    @Resource
    PeerManager peerManager;
    
    @Resource
    P2PMessageEngine p2pMessageEngine;

    @Resource(name = "produceToml")
    Toml toml;

    @Bean
    public WeCrossHost newWeCrossHost() {
        WeCrossHost host = new WeCrossHost();
        host.setZoneManager(zoneManager);
        host.setP2pService(p2pService);
        host.setPeerManager(peerManager);
        
        // set the p2p engine here to avoid circular reference
        zoneManager.setP2PEngine(p2pMessageEngine);
        
        host.start();
        return host;
    }
}
