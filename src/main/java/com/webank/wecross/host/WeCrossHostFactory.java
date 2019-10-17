package com.webank.wecross.host;

import com.webank.wecross.network.NetworkManager;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WeCrossHostFactory {
    @Resource NetworkManager networkManager;

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
