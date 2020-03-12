package com.webank.wecross.config;

import com.webank.wecross.zone.Zone;
import com.webank.wecross.zone.ZoneManager;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZoneManagerConfig {
    @Resource(name = "newZoneMap")
    private Map<String, Zone> zones;

    @Bean
    public ZoneManager newZoneManager() {
        ZoneManager zoneManager = new ZoneManager();
        zoneManager.setZones(zones);
        return zoneManager;
    }

    public Map<String, Zone> getNetworks() {
        return zones;
    }

    public void setNetworks(Map<String, Zone> networks) {
        this.zones = networks;
    }
}
