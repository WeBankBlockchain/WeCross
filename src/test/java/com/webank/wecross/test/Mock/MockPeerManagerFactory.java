package com.webank.wecross.test.Mock;

import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.netty.P2PService;
import com.webank.wecross.peer.PeerManager;
import com.webank.wecross.zone.ZoneManager;

public class MockPeerManagerFactory {
    public static PeerManager newMockPeerManager(
            ZoneManager networkManager, P2PService p2pService, P2PMessageEngine p2pEngine) {
        PeerManager manager = new PeerManager();

        manager.setPeerActiveTimeout(17000);
        return manager;
    }
}
