package com.webank.wecross.host;

import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.P2PMessageEngineFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SyncMessageHandlerFactory {
    private P2PMessageEngine p2pEngine;

    @Bean
    public SyncPeerMessageHandler newSyncPeerMessageHandler() {
        SyncPeerMessageHandler handler = new SyncPeerMessageHandler();
        handler.setP2pEngine(getP2pEngine());
        return handler;
    }

    public void setP2pEngine(P2PMessageEngine p2pEngine) {
        this.p2pEngine = p2pEngine;
    }

    public P2PMessageEngine getP2pEngine() {
        if (p2pEngine == null) {
            p2pEngine = P2PMessageEngineFactory.getEngineInstance();
        }
        return p2pEngine;
    }
}
