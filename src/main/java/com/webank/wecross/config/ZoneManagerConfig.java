package com.webank.wecross.config;

import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stubmanager.MemoryBlockManagerFactory;
import com.webank.wecross.stubmanager.StubManager;
import com.webank.wecross.zone.Chain;
import com.webank.wecross.zone.Zone;
import com.webank.wecross.zone.ZoneManager;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZoneManagerConfig {
    @Resource(name = "newZoneMap")
    private Map<String, Zone> zones;

    @Resource private StubManager stubManager;

    @Resource private MemoryBlockManagerFactory resourceBlockManagerFactory;

    @Resource private BlockVerifierTomlConfig.Verifiers verifiers;

    @Bean
    public ZoneManager newZoneManager() {
        System.out.println("Initializing ZoneManager ...");

        ZoneManager zoneManager = new ZoneManager();
        zoneManager.setZones(zones);
        zoneManager.setStubManager(stubManager);
        zoneManager.setMemoryBlockManagerFactory(resourceBlockManagerFactory);
        zoneManager.setVerifiers(verifiers);
        addChangeEvent(zoneManager);

        return zoneManager;
    }

    private void addChangeEvent(ZoneManager zoneManager) {
        Map<String, Zone> zones = zoneManager.getZones();
        for (Zone zone : zones.values()) {
            Map<String, Chain> chains = zone.getChains();
            for (Chain chain : chains.values()) {
                Connection localConnection = chain.getLocalConnection();
                if (localConnection != null) {
                    localConnection.setConnectionEventHandler(
                            new Connection.ConnectionEventHandler() {
                                @Override
                                public void onResourcesChange(List<ResourceInfo> resourceInfos) {
                                    chain.updateLocalResources(resourceInfos);
                                    zoneManager.newSeq();
                                }
                            });
                }
            }
        }
    }
}
