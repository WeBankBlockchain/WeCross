package com.webank.wecross.core.config;

import com.webank.wecross.core.NetworkManager;
import com.webank.wecross.network.Network;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NetworkManagerConfig {

    @Resource private Map<String, Network> networks;

    @Bean
    public NetworkManager getNetworkManager() {
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
