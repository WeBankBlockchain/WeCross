package com.webank.wecross.host.config;

import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.network.NetworkManager;
import com.webank.wecross.peer.PeerManager;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WeCrossHostFactory {

    @Resource(name = "newNetworkManager")
    NetworkManager networkManager;

    @Resource(name = "newPeerManager")
    PeerManager peerManager;

    @Bean
    public WeCrossHost newWeCrossHost() {
        WeCrossHost host = new WeCrossHost();
        host.setNetworkManager(networkManager);
        host.setPeerManager(peerManager);
        host.start();
        return host;
    }
}
