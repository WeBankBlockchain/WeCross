package com.webank.wecross.config;

import com.webank.wecross.stub.StubManager;
import com.webank.wecross.zone.Zone;
import com.webank.wecross.zone.ZoneManager;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZoneManagerConfig {
    @Resource(name = "zoneConfig")
    private Map<String, Zone> zones;
    
    @Resource
    private StubManager stubManager;

    @Bean
    public ZoneManager newZoneManager() {
        ZoneManager zoneManager = new ZoneManager();
        zoneManager.setZones(zones);
        zoneManager.setStubManager(stubManager);
        return zoneManager;
    }
}
