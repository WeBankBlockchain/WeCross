package com.webank.wecross.host;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SyncMessageHandlerFactory {
    @Bean
    public SyncPeerMessageHandler newSyncPeerMessageHandler() {
        return new SyncPeerMessageHandler();
    }
}
