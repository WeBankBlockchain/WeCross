package com.webank.wecross.network.config;

import com.webank.wecross.network.Network;
import com.webank.wecross.network.NetworkManager;
import com.webank.wecross.p2p.P2PMessageEngine;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NetworkManagerFactory {
    @Resource private Map<String, Network> networks;

    @Resource(name = "newP2PMessageEngine")
    private P2PMessageEngine p2pEngine;

    @Bean
    public NetworkManager newNetworkManager() {
        NetworkManager networkManager = new NetworkManager();
        networkManager.setNetworks(networks);
        networkManager.setP2pEngine(p2pEngine);
        return networkManager;
    }

    public Map<String, Network> getNetworks() {
        return networks;
    }

    public void setNetworks(Map<String, Network> networks) {
        this.networks = networks;
    }
}
