package com.webank.wecross.host;

import com.webank.wecross.account.AccountManager;
import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.netty.P2PService;
import com.webank.wecross.p2p.netty.common.Node;
import com.webank.wecross.peer.Peer;
import com.webank.wecross.peer.PeerManager;
import com.webank.wecross.peer.PeerSeqMessageData;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.restserver.Versions;
import com.webank.wecross.restserver.request.StateRequest;
import com.webank.wecross.restserver.response.StateResponse;
import com.webank.wecross.routine.RoutineManager;
import com.webank.wecross.stub.Path;
import com.webank.wecross.zone.ZoneManager;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeCrossHost {
    private Logger logger = LoggerFactory.getLogger(WeCrossHost.class);

    private ZoneManager zoneManager;
    private PeerManager peerManager;
    private P2PService p2pService;
    private AccountManager accountManager;
    private RoutineManager routineManager;

    Thread mainLoopThread;

    public void start() {
        /* start netty p2p service */
        try {
            check();

            /** start netty p2p service */
            p2pService.start();

            // start main loop
            mainLoopThread =
                    new Thread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    mainLoop();
                                }
                            });
            mainLoopThread.start();
        } catch (Exception e) {
            logger.error("Startup host error: {}", e);
            System.exit(-1);
        }
    }

    public void mainLoop() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("Thread exception", e);
            }

            broadcastResourceSeq();

            dumpStatus();

            logger.info("WeCross router is running");
        }
    }

    public void check() throws Exception {
        if (zoneManager == null) {
            throw new Exception("zoneManager is null");
        }
        if (peerManager == null) {
            throw new Exception("peerManager is null");
        }
        if (p2pService == null) {
            throw new Exception("p2pService is null");
        }
        if (accountManager == null) {
            throw new Exception("accountManager is null");
        }
    }

    private void broadcastResourceSeq() {
        int seq = zoneManager.getSeq();

        PeerSeqMessageData peerSeqMessageData = new PeerSeqMessageData();
        peerSeqMessageData.setSeq(seq);

        P2PMessage<Object> msg = new P2PMessage<>();
        msg.newSeq();
        msg.setData(peerSeqMessageData);
        msg.setVersion(Versions.currentVersion);
        msg.setMethod("seq");

        for (Peer peer : peerManager.getPeerInfos().values()) {
            logger.debug("Send peer seq, to peer:{}, seq:{}", peer, msg.getSeq());
            zoneManager.getP2PEngine().asyncSendMessage(peer, msg, null);
        }
    }

    private void dumpStatus() {
        dumpPeers();
        dumpActiveResources();
    }

    private void dumpActiveResources() {
        Map<String, Resource> activeResources = getZoneManager().getAllResources(false);
        String dumpStr = "Current active resources: [";
        for (Map.Entry<String, Resource> entry : activeResources.entrySet()) {
            String path = entry.getKey();
            dumpStr += path;
            if (entry.getValue().isHasLocalConnection()) {
                dumpStr += "(local)";
            } else {
                dumpStr += "(remote)";
            }
            dumpStr += ", ";
        }
        dumpStr += "]";
        logger.debug(dumpStr);
    }

    private void dumpPeers() {
        Map<Node, Peer> peerInfos = getPeerManager().getPeerInfos();
        String dumpStr = "Current peers: [";
        for (Node node : peerInfos.keySet()) {
            dumpStr += node.toString() + ", ";
        }

        dumpStr += "]";
        logger.debug(dumpStr);
    }

    public Resource getResource(Path path) throws Exception {
        return zoneManager.getResource(path);
    }

    public StateResponse getState(StateRequest request) {
        StateResponse response = new StateResponse();
        response.setSeq(zoneManager.getSeq());
        return response;
    }

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public void setAccountManager(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    public void setZoneManager(ZoneManager networkManager) {
        this.zoneManager = networkManager;
    }

    public ZoneManager getZoneManager() {
        return this.zoneManager;
    }

    public PeerManager getPeerManager() {
        return peerManager;
    }

    public void setPeerManager(PeerManager peerManager) {
        this.peerManager = peerManager;
    }

    public P2PService getP2pService() {
        return p2pService;
    }

    public void setP2pService(P2PService p2pService) {
        this.p2pService = p2pService;
    }

    public RoutineManager getRoutineManager() {
        return routineManager;
    }

    public void setRoutineManager(RoutineManager routineManager) {
        this.routineManager = routineManager;
    }
}
