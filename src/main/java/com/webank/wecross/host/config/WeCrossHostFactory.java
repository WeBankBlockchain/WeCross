package com.webank.wecross.host.config;

import com.moandjiezana.toml.Toml;
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

    @Resource(name = "produceToml")
    Toml toml;

    @Bean
    public WeCrossHost newWeCrossHost() {
        WeCrossHost host = new WeCrossHost();
        host.setNetworkManager(networkManager);
        host.setPeerManager(peerManager);
        host.setEnableTestResource(enableTestResource());
        host.start();
        return host;
    }

    private boolean enableTestResource() {
        try {
            return toml.getBoolean("test.enableTestResource");
        } catch (Exception e) {
            return false;
        }
    }
}
