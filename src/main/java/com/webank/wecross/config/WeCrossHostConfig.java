package com.webank.wecross.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.p2p.netty.P2PService;
import com.webank.wecross.peer.PeerManager;
import com.webank.wecross.zone.ZoneManager;

import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WeCrossHostConfig {

    @Resource(name = "newNetworkManager")
    ZoneManager zoneManager;
    
    @Resource
    P2PService p2pService;

    @Resource(name = "produceToml")
    Toml toml;

    @Bean
    public WeCrossHost newWeCrossHost() {
        WeCrossHost host = new WeCrossHost();
        host.setZoneManager(zoneManager);
        host.setP2pService(p2pService);
        host.start();
        return host;
    }
}
