package com.webank.wecross.peer;

import com.webank.wecross.core.SeqUtils;
import com.webank.wecross.network.NetworkManager;
import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.netty.P2PService;
import com.webank.wecross.p2p.netty.common.Peer;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.resource.TestResource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeerManager {
    Logger logger = LoggerFactory.getLogger(PeerManager.class);

    private NetworkManager networkManager;

    private P2PMessageEngine p2pEngine;

    private Map<Peer, PeerInfo> peerInfos = new HashMap<>(); // peer

    private int seq = 1; // Seq of the host

    private SyncPeerMessageHandler messageHandler;

    private P2PService p2PService;

    private long peerActiveTimeout;

    private Set<String> activeResources = new HashSet<>();

    public void start() {
        newSeq();

        final long timeInterval = 5000;
        Runnable runnable =
                new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            try {
                                workLoop();
                                Thread.sleep(timeInterval);
                            } catch (Exception e) {
                                logger.error("Startup error: " + e);
                                System.exit(-1);
                            }
                        }
                    }
                };

        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void newSeq() {
        seq = this.seq = SeqUtils.newSeq();
    }

    public int getSeq() {
        return seq;
    }

    public int peerSize() {
        return peerInfos.size();
    }

    public synchronized PeerInfo getPeerInfo(Peer peer) {
        if (peerInfos.containsKey(peer)) {
            return peerInfos.get(peer);
        } else {
            return null;
        }
    }

    public synchronized void updatePeerInfo(PeerInfo peerInfo) {
        peerInfos.put(peerInfo.getPeer(), peerInfo);
    }

    public synchronized void removePeerInfo(Peer peer) {
        peerInfos.remove(peer);
    }

    public synchronized void clearPeerInfos() {
        peerInfos.clear();
    }

    public synchronized void notePeerActive(Peer peer) {
        PeerInfo peerInfo = getPeerInfo(peer);
        if (peerInfo == null) {
            updatePeerInfo(new PeerInfo(peer));
        }
        getPeerInfo(peer).noteAlive();
    }

    public synchronized boolean hasPeerChanged(Peer peer, int currentSeq) {
        if (!peerInfos.containsKey(peer)) {
            return true;
        }
        return peerInfos.get(peer).getSeq() != currentSeq;
    }

    public Object onRestfulPeerMessage(String method, P2PMessage msg) {
        switch (method) {
            case "requestSeq":
                {
                    return handleRequestSeq();
                }
            case "requestPeerInfo":
                {
                    return handleRequestPeerInfo();
                }
            default:
                messageHandler.onPeerMessage(new Peer(), method, msg);
        }
        return null;
    }

    public void broadcastSeqRequest() {
        for (PeerInfo peerInfo : peerInfos.values()) {
            sendSeqRequest(peerInfo.getPeer());
        }
    }

    public void sendSeqRequest(Peer peer) {
        P2PMessage<Object> msg = new P2PMessage<>();
        msg.newSeq();
        msg.setData(null);
        msg.setVersion("0.2");
        msg.setMethod("requestSeq");

        PeerSeqCallback callback = new PeerSeqCallback();
        callback.setHandler(messageHandler);
        callback.setPeer(peer);

        logger.info("Request peer seq, peer:{}, seq:{}", peer, msg.getSeq());
        p2pEngine.asyncSendMessage(peer, msg, callback);
    }

    public void broadcastPeerInfoRequest() {
        for (PeerInfo peerInfo : peerInfos.values()) {
            sendPeerInfoRequest(peerInfo.getPeer());
        }
    }

    public void sendPeerInfoRequest(Peer peer) {
        sendPeerInfoRequest(peer, 0);
    }

    public void sendPeerInfoRequest(Peer peer, int seq) {

        P2PMessage<Object> msg = new P2PMessage<>();

        if (seq == 0) {
            msg.newSeq();
        } else {
            msg.setSeq(seq);
        }

        msg.setData(null);
        msg.setVersion("0.1");
        msg.setMethod("requestPeerInfo");

        PeerInfoCallback callback = new PeerInfoCallback();
        callback.setHandler(messageHandler);
        callback.setPeer(peer);

        logger.info("Request peer info, peer:{}, seq:{}", peer, msg.getSeq());
        p2pEngine.asyncSendMessage(peer, msg, callback);
    }

    public PeerSeqMessageData handleRequestSeq() {
        PeerSeqMessageData data = new PeerSeqMessageData();
        data.setSeq(seq);
        return data;
    }

    public void handleSeq(Peer peer, P2PMessage msg) {
        logger.info("Receive peer seq from peer:{}", peer);
        notePeerActive(peer);

        PeerSeqMessageData data = (PeerSeqMessageData) msg.getData();
        if (data != null && msg.getMethod().equals("seq")) {
            int currentSeq = data.getSeq();
            if (hasPeerChanged(peer, currentSeq)) {

                sendPeerInfoRequest(peer, msg.getSeq() + 1);
            }
        } else {
            logger.warn("Receive unrecognized seq message from peer:" + peer);
        }
    }

    public PeerInfoMessageData handleRequestPeerInfo() {
        logger.info("Receive request peer info");
        PeerInfoMessageData data = new PeerInfoMessageData();
        data.setSeq(seq);
        data.setResources(activeResources);

        logger.info("Respond peerInfo to peer, resource:" + activeResources);
        return data;
    }

    public void handlePeerInfo(Peer peer, P2PMessage msg) {
        logger.info("Receive peer info from {}", peer);
        notePeerActive(peer);

        PeerInfoMessageData data = (PeerInfoMessageData) msg.getData();
        if (data != null && msg.getMethod().equals("peerInfo")) {
            int currentSeq = data.getSeq();
            if (hasPeerChanged(peer, currentSeq)) {
                // compare and update
                Set<String> currentResources = data.getResources();
                logger.info(
                        "Update peerInfo from {}, seq:{}, resource:{}",
                        peer,
                        currentSeq,
                        currentResources);

                PeerInfo peerRecord = getPeerInfo(peer);
                if (peerRecord == null) {
                    peerRecord = new PeerInfo(peer);
                    peerRecord.setResources(currentSeq, currentResources);
                    updatePeerInfo(peerRecord);
                } else {
                    peerRecord.setResources(currentSeq, currentResources);
                }
                //
                syncWithPeerNetworks();
            } else {
                logger.info("Peer info not changed, seq:{}", currentSeq);
            }
        } else {
            logger.warn("Receive unrecognized seq message from peer:" + peer);
        }
    }

    public Set<String> getAllPeerResource() {
        Set<String> ret = new HashSet<>();
        for (PeerInfo peerInfo : peerInfos.values()) {
            if (peerInfo.getResources() != null) {
                for (String resource : peerInfo.getResources()) {
                    ret.add(resource);
                }
            }
        }
        return ret;
    }

    public void setP2pEngine(P2PMessageEngine p2pEngine) {
        this.p2pEngine = p2pEngine;
    }

    public void setPeerInfos(Map<Peer, PeerInfo> peerInfos) {
        this.peerInfos = peerInfos;
    }

    public synchronized Set<PeerInfo> getActivePeerInfos() {
        Set<PeerInfo> ret = new HashSet<>();
        for (PeerInfo peerInfo : peerInfos.values()) {
            if (!peerInfo.isTimeout(peerActiveTimeout)) {
                ret.add(peerInfo);
            }
        }
        return ret;
    }

    public void setMessageHandler(SyncPeerMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    public long getPeerActiveTimeout() {
        return peerActiveTimeout;
    }

    public void setPeerActiveTimeout(long peerActiveTimeout) {
        this.peerActiveTimeout = peerActiveTimeout;
    }

    public Set<String> getActiveResources() {
        return activeResources;
    }

    public void setActiveResources(Set<String> activeResources) {
        if (!this.activeResources.equals(activeResources)) {
            this.newSeq();
            this.activeResources = activeResources;
            logger.info(
                    "Update active resources newSeq:{}, resource:{}", seq, this.activeResources);
        }
    }

    public void setNetworkManager(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    public Set<Peer> getConnectedPeers() {
        return getP2PService().getConnections().getPeers();
    }

    public void syncWithPeerNetworks() {
        // Update peers' resource into networks
        Set<PeerInfo> activePeers = this.getActivePeerInfos();
        networkManager.updateActivePeerNetwork(activePeers);

        // Log all active resources
        Set<String> activeResources = networkManager.getAllNetworkStubResourceName(false);
        logger.info("Current active resource:" + activeResources);

        // Update active resource back to peerManager
        Set<String> activeLocalResources = networkManager.getAllNetworkStubResourceName(true);
        logger.info("Current active local resources:" + activeLocalResources);
        this.setActiveResources(activeLocalResources);
    }

    public void maintainPeerConnections() {
        Set<Peer> connectedPeers = getConnectedPeers();

        Set<Peer> peers2Add = new HashSet<>(connectedPeers);
        peers2Add.removeAll(peerInfos.keySet());

        Set<Peer> peers2Remove = new HashSet<>(peerInfos.keySet());
        peers2Remove.removeAll(connectedPeers);

        if (peers2Add != null) {
            for (Peer peer : peers2Add) {
                updatePeerInfo(new PeerInfo(peer));
            }
        }

        if (peers2Remove != null) {
            for (Peer peer : peers2Remove) {
                removePeerInfo(peer);
            }
        }

        logger.info("Current connected peers: {}", peerInfos.keySet());
    }

    private void workLoop() {
        try {
            addMockResources();
            maintainPeerConnections();
            syncWithPeerNetworks();
            broadcastSeqRequest();
        } catch (Exception e) {
            logger.warn("workloop exception: {}", e);
        }
    }

    private void addMockResources() {
        try {
            Long timestamp = System.currentTimeMillis();
            logger.info("Add test resource");
            Path path =
                    Path.decode(
                            "test-network"
                                    + ((timestamp / 1000) % 10)
                                    + ".test-stub"
                                    + ((timestamp / 100) % 100)
                                    + ".test-resource"
                                    + timestamp % 100);
            Resource resource = new TestResource();
            resource.setPath(path);
            networkManager.addResource(resource);

        } catch (Exception e) {
            logger.warn("Add test resource exception " + e);
        }
    }

    public P2PService getP2PService() {
        return p2PService;
    }

    public void setP2PService(P2PService p2PService) {
        this.p2PService = p2PService;
    }
}
