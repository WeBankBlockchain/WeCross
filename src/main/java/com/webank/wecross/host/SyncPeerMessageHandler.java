package com.webank.wecross.host;

import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.P2PMessageEngine;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class SyncPeerMessageHandler {
    @Resource(name = "newPeerManager")
    private PeerManager peerManager;

    @Resource(name = "newRestfulP2PMessageEngine")
    private P2PMessageEngine p2pEngine;

    Logger logger = LoggerFactory.getLogger(SyncPeerMessageHandler.class);
    private ThreadPoolTaskExecutor threadPool;

    public SyncPeerMessageHandler() {
        final int threadNum = 1;
        threadPool = new ThreadPoolTaskExecutor();
        threadPool.setCorePoolSize(threadNum);
        threadPool.setMaxPoolSize(threadNum);
        threadPool.initialize();
    }

    public void onSyncPeerMessage(String url, String method, P2PMessage msg) {
        threadPool.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            handleSyncPeerMessage(url, method, msg);
                        } catch (Exception e) {
                            logger.warn(
                                    "handleSyncPeerMessage exception:{} url:{} method:{} msg:{}",
                                    e.toString(),
                                    url,
                                    method,
                                    msg);
                        }
                    }
                });
    }

    private void handleSyncPeerMessage(String url, String method, P2PMessage msg) throws Exception {
        logger.info("Receive peer message url:{}, method:{}, msg:{}", url, method, msg);
        switch (method) {
            case "requestSeq":
                {
                    handleRequestSeq(url, msg);
                    break;
                }
            case "seq":
                {
                    handleSeq(url, msg);
                    break;
                }
            case "requestPeerInfo":
                {
                    handleRequestPeerInfo(url, msg);
                    break;
                }
            case "peerInfo":
                {
                    handlePeerInfo(url, msg);
                    break;
                }
            default:
                {
                    throw new Exception("Unrecognized peer message method: " + method);
                }
        }
    }

    private void handleRequestSeq(String url, P2PMessage msg) {
        logger.error("Reach invalid function");
    }

    private void handleSeq(String url, P2PMessage msg) {
        logger.error("Reach invalid function");
    }

    private void handleRequestPeerInfo(String url, P2PMessage msg) {
        logger.error("Reach invalid function");
    }

    private void handlePeerInfo(String url, P2PMessage msg) {
        logger.error("Reach invalid function");
    }

    @Bean
    public SyncPeerMessageHandler newSyncPeerMessageHandler() {
        return new SyncPeerMessageHandler();
    }
}
