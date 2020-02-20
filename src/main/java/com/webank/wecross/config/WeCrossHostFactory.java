package com.webank.wecross.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.peer.PeerManager;
import com.webank.wecross.zone.ZoneManager;

import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WeCrossHostFactory {

    @Resource(name = "newNetworkManager")
    ZoneManager networkManager;

    @Resource(name = "newPeerManager")
    PeerManager peerManager;

    @Resource(name = "produceToml")
    Toml toml;

    @Bean
    public WeCrossHost newWeCrossHost() {
        WeCrossHost host = new WeCrossHost();
        host.setNetworkManager(networkManager);
        host.setPeerManager(peerManager);
        host.start();
        return host;
    }
}
