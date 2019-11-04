package com.webank.wecross.peer.config;

import com.webank.wecross.network.NetworkManager;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.Peer;
import com.webank.wecross.peer.PeerInfo;
import com.webank.wecross.peer.PeerManager;
import com.webank.wecross.peer.SyncPeerMessageHandler;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PeerManagerFactory {
    @Resource(name = "newNetworkManager")
    private NetworkManager networkManager;

    @Resource(name = "newP2PMessageEngine")
    private P2PMessageEngine p2pEngine;

    @Resource(name = "initPeers")
    private Map<String, Peer> peers; // url -> peer

    @Resource(name = "newSyncPeerMessageHandler")
    private SyncPeerMessageHandler messageHandler;

    private long peerActiveTimeout = 17000; // 17s

    @Bean
    public PeerManager newPeerManager() {
        PeerManager manager = new PeerManager();
        manager.setP2pEngine(p2pEngine);
        messageHandler.setPeerManager(manager);
        manager.setNetworkManager(networkManager);
        manager.setMessageHandler(messageHandler);
        manager.setPeerActiveTimeout(peerActiveTimeout);

        // delete me after netty ready
        Map<Peer, PeerInfo> peerInfos = new HashMap<>();
        for (Peer peer : peers.values()) {
            peerInfos.putIfAbsent(peer, new PeerInfo(peer));
        }
        manager.setPeerInfos(peerInfos);
        // delete me after netty ready

        return manager;
    }

    public void setMessageHandler(SyncPeerMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    public void setPeers(Map<String, Peer> peers) {
        this.peers = peers;
    }
}
