package com.webank.wecross.config;

import com.webank.wecross.storage.BlockHeaderStorageFactory;
import com.webank.wecross.stub.StubManager;
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

    @Resource private StubManager stubManager;

    @Resource private BlockHeaderStorageFactory blockHeaderStorageFactory;

    @Bean
    public ZoneManager newZoneManager() {
        System.out.println("Initializing ZoneManager ...");

        ZoneManager zoneManager = new ZoneManager();
        zoneManager.setZones(zones);
        zoneManager.setStubManager(stubManager);
        zoneManager.setBlockHeaderStorageFactory(blockHeaderStorageFactory);
        return zoneManager;
    }
}
