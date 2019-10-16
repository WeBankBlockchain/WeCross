package com.webank.wecross.host;

import com.webank.wecross.p2p.P2PMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class SyncPeerMessageHandler {
    Logger logger = LoggerFactory.getLogger(SyncPeerMessageHandler.class);
    private ThreadPoolTaskExecutor threadPool;

    public SyncPeerMessageHandler() {
        final int threadNum = 1;
        threadPool = new ThreadPoolTaskExecutor();
        threadPool.setCorePoolSize(threadNum);
        threadPool.setMaxPoolSize(threadNum);
        threadPool.initialize();
    }

    public void onSyncPeerMessage(String method, P2PMessage msg) {
        threadPool.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            handleSyncPeerMessage(method, msg);
                        } catch (Exception e) {

                        }
                    }
                });
    }

    private void handleSyncPeerMessage(String method, P2PMessage msg) {
        logger.info("Receive peer message method:{}, msg:{}", method, msg);
    }

    @Bean
    public SyncPeerMessageHandler newSyncPeerMessageHandler() {
        return new SyncPeerMessageHandler();
    }
}
