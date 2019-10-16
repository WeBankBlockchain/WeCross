package com.webank.wecross.network;

import java.util.Map;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NetworkManagerFactory {
    @Resource private Map<String, Network> networks;

    @Bean
    public NetworkManager newNetworkManager() {
        NetworkManager networkManager = new NetworkManager();
        networkManager.setNetworks(networks);
        return networkManager;
    }

    public Map<String, Network> getNetworks() {
        return networks;
    }

    public void setNetworks(Map<String, Network> networks) {
        this.networks = networks;
    }
}
