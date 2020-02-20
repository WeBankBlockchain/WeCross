package com.webank.wecross.test.Mock;

import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.zone.Zone;
import com.webank.wecross.zone.ZoneManager;

import java.util.HashMap;
import java.util.Map;

public class MockNetworkManagerFactory {
    public static ZoneManager newMockNteworkManager(P2PMessageEngine p2pEngine) {
        ZoneManager manager = new ZoneManager();
        Map<String, Zone> networks = new HashMap<>();
        manager.setNetworks(networks);
        manager.setP2pEngine(p2pEngine);
        return manager;
    }
}
