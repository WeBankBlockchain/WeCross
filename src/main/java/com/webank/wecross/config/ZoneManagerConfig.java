package com.webank.wecross.config;

import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.zone.Zone;
import com.webank.wecross.zone.ZoneManager;

import java.util.Map;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZoneManagerConfig {
    @Resource(name = "zoneConfig")
    private Map<String, Zone> networks;

    @Resource
    private P2PMessageEngine p2pEngine;

    @Bean
    public ZoneManager newNetworkManager() {
        ZoneManager zoneManager = new ZoneManager();
        zoneManager.setNetworks(networks);
        zoneManager.setP2pEngine(p2pEngine);
        return zoneManager;
    }

    public Map<String, Zone> getNetworks() {
        return networks;
    }

    public void setNetworks(Map<String, Zone> networks) {
        this.networks = networks;
    }
}
