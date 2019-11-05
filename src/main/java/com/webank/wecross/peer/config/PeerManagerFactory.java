package com.webank.wecross.peer.config;

import com.webank.wecross.network.NetworkManager;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.netty.P2PService;
import com.webank.wecross.peer.PeerManager;
import com.webank.wecross.peer.SyncPeerMessageHandler;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PeerManagerFactory {
    @Resource(name = "newNetworkManager")
    private NetworkManager networkManager;

    @Resource(name = "newP2PMessageEngine")
    private P2PMessageEngine p2pEngine;

    @Resource(name = "newSyncPeerMessageHandler")
    private SyncPeerMessageHandler messageHandler;

    @Resource(name = "newP2PService")
    private P2PService p2PService;

    private long peerActiveTimeout = 17000; // 17s

    @Bean
    public PeerManager newPeerManager() {

        PeerManager manager = new PeerManager();
        manager.setP2pEngine(p2pEngine);
        messageHandler.setPeerManager(manager);
        manager.setNetworkManager(networkManager);
        manager.setMessageHandler(messageHandler);
        manager.setPeerActiveTimeout(peerActiveTimeout);
        manager.setP2PService(p2PService);

        p2PService
                .getChannelHandlerCallBack()
                .getCallBack()
                .getResourceRequestProcessor()
                .setPeerManager(manager);

        p2PService
                .getChannelHandlerCallBack()
                .getCallBack()
                .getResourceRequestProcessor()
                .setNetworkManager(networkManager);

        return manager;
    }

    public void setMessageHandler(SyncPeerMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }
}
