package com.webank.wecross.test.Mock;

import com.webank.wecross.network.Network;
import com.webank.wecross.network.NetworkManager;
import com.webank.wecross.p2p.P2PMessageEngine;
import java.util.HashMap;
import java.util.Map;

public class MockNetworkManagerFactory {
    public static NetworkManager newMockNteworkManager(P2PMessageEngine p2pEngine) {
        NetworkManager manager = new NetworkManager();
        Map<String, Network> networks = new HashMap<>();
        manager.setNetworks(networks);
        manager.setP2pEngine(p2pEngine);
        return manager;
    }
}
