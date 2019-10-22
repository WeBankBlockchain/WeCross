package com.webank.wecross.host;

import com.webank.wecross.p2p.P2PMessageEngine;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SyncMessageHandlerFactory {
    @Resource(name = "newRestfulP2PMessageEngine")
    private P2PMessageEngine p2pEngine;

    @Bean
    public SyncPeerMessageHandler newSyncPeerMessageHandler() {
        SyncPeerMessageHandler handler = new SyncPeerMessageHandler();
        handler.setP2pEngine(p2pEngine);
        return handler;
    }
}
