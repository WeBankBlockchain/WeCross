package com.webank.wecross.peer;

import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.netty.common.Peer;
import com.webank.wecross.restserver.Versions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class SyncPeerMessageHandler {
    private PeerManager peerManager;

    private P2PMessageEngine p2pEngine;

    Logger logger = LoggerFactory.getLogger(SyncPeerMessageHandler.class);
    private ThreadPoolTaskExecutor threadPool;

    public SyncPeerMessageHandler() {
        final int threadNum = 1; // Just use 1 thread as a queue to be synchronized
        threadPool = new ThreadPoolTaskExecutor();
        threadPool.setCorePoolSize(threadNum);
        threadPool.setMaxPoolSize(threadNum);
        threadPool.initialize();
    }

    public void onPeerMessage(Peer peer, String method, P2PMessage msg) {
        threadPool.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            handlePeerMessage(peer, method, msg);
                        } catch (Exception e) {
                            logger.warn(
                                    "handleSyncPeerMessage exception:{} peer:{} method:{} msg:{}",
                                    e,
                                    peer,
                                    method,
                                    msg);
                        }
                    }
                });
    }

    private void handlePeerMessage(Peer peer, String method, P2PMessage msg) throws Exception {
        logger.trace("Receive peer message peer:{}, method:{}, msg:{}", peer, method, msg);
        switch (method) {
            case "requestSeq":
                {
                    handleRequestSeq(peer, msg);
                    break;
                }
            case "seq":
                {
                    handleSeq(peer, msg);
                    break;
                }
            case "requestPeerInfo":
                {
                    handleRequestPeerInfo(peer, msg);
                    break;
                }
            case "peerInfo":
                {
                    handlePeerInfo(peer, msg);
                    break;
                }
            default:
                {
                    throw new Exception("Unrecognized peer message method: " + method);
                }
        }
    }

    private void handleRequestSeq(Peer peer, P2PMessage msg) {
        PeerSeqMessageData data = peerManager.handleRequestSeq();
        P2PMessage<PeerSeqMessageData> rspMsg = new P2PMessage<>();
        rspMsg.setVersion(Versions.currentVersion);
        rspMsg.setSeq(msg.getSeq());
        rspMsg.setMethod("com/webank/wecross/peer/seq");
        rspMsg.setData(data);

        p2pEngine.asyncSendMessage(peer, rspMsg, new PeerDoNothingCallback());
    }

    private void handleSeq(Peer peer, P2PMessage msg) {
        peerManager.handleSeq(peer, msg);
    }

    private void handleRequestPeerInfo(Peer peer, P2PMessage msg) {
        PeerInfoMessageData data = peerManager.handleRequestPeerInfo();
        P2PMessage<PeerInfoMessageData> rspMsg = new P2PMessage<>();
        rspMsg.setVersion(Versions.currentVersion);
        rspMsg.setSeq(msg.getSeq());
        rspMsg.setMethod("com/webank/wecross/peer/peerInfo");
        rspMsg.setData(data);
        p2pEngine.asyncSendMessage(peer, rspMsg, new PeerDoNothingCallback());
    }

    private void handlePeerInfo(Peer peer, P2PMessage msg) {
        peerManager.handlePeerInfo(peer, msg);
    }

    public void setPeerManager(PeerManager peerManager) {
        this.peerManager = peerManager;
    }

    public void setP2pEngine(P2PMessageEngine p2pEngine) {
        this.p2pEngine = p2pEngine;
    }
}
