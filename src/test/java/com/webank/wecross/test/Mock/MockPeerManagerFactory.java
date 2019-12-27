package com.webank.wecross.test.Mock;

import com.webank.wecross.network.NetworkManager;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.netty.P2PService;
import com.webank.wecross.peer.PeerManager;
import com.webank.wecross.peer.SyncPeerMessageHandler;

public class MockPeerManagerFactory {
    public static PeerManager newMockPeerManager(
            NetworkManager networkManager, P2PService p2pService, P2PMessageEngine p2pEngine) {
        PeerManager manager = new PeerManager();
        manager.setP2pEngine(p2pEngine);

        SyncPeerMessageHandler messageHandler = new SyncPeerMessageHandler();
        messageHandler.setP2pEngine(p2pEngine);
        messageHandler.setPeerManager(manager);
        manager.setNetworkManager(networkManager);
        manager.setMessageHandler(messageHandler);

        manager.setPeerActiveTimeout(17000);
        manager.setP2PService(p2pService);
        return manager;
    }
}
