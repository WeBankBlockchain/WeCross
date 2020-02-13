package com.webank.wecross.peer;

import static com.webank.wecross.resource.ResourceInfo.isEqualInfos;

import com.webank.wecross.network.NetworkManager;
import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.netty.P2PService;
import com.webank.wecross.p2p.netty.common.Node;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.resource.ResourceInfo;
import com.webank.wecross.resource.TestResource;
import com.webank.wecross.restserver.Versions;
import com.webank.wecross.utils.core.SeqUtils;
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

    private Map<Node, PeerInfo> peerInfos = new HashMap<Node, PeerInfo>(); // peer

    private int seq = 1; // Seq of the host

    private SyncPeerMessageHandler messageHandler;

    private P2PService p2PService;

    private long peerActiveTimeout;

    private Map<String, ResourceInfo> activeResources = new HashMap<>();

    public void start() {
        newSeq();

        final long timeInterval = 5000;
        Runnable runnable =
                new Runnable() {
                    @Override
                    public void run() {
                        boolean running = true;
                        while (running) {
                            try {
                                workLoop();
                                Thread.sleep(timeInterval);
                            } catch (Exception e) {
                                logger.error("Startup error: " + e);
                                running = false;
                            }
                        }
                        System.exit(-1);
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

    public NetworkManager getNetworkManager() {
        return this.networkManager;
    }

    public int peerSize() {
        return peerInfos.size();
    }

    public synchronized PeerInfo getPeerInfo(Node node) {
        if (peerInfos.containsKey(node)) {
            return peerInfos.get(node);
        } else {
            return null;
        }
    }

    public synchronized void updatePeerInfo(PeerInfo peerInfo) {
        peerInfos.put(peerInfo.getNode(), peerInfo);
    }

    public synchronized void removePeerInfo(Node node) {
        peerInfos.remove(node);
    }

    public synchronized void clearPeerInfos() {
        peerInfos.clear();
    }

    public synchronized void notePeerActive(PeerInfo peer) {
        PeerInfo peerInfo = getPeerInfo(peer.getNode());
        if (peerInfo == null) {
            updatePeerInfo(peer);
        }
        peer.noteAlive();
    }

    public synchronized boolean hasPeerChanged(Node node, int currentSeq) {
        if (!peerInfos.containsKey(node)) {
            return true;
        }
        return peerInfos.get(node).getSeq() != currentSeq;
    }

    public Object onRestfulPeerMessage(PeerInfo peer, String method, P2PMessage msg) {
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
                messageHandler.onPeerMessage(peer, method, msg);
        }
        return null;
    }

    public void broadcastSeqRequest() {
        for (PeerInfo peerInfo : peerInfos.values()) {
            sendSeqRequest(peerInfo);
        }
    }

    public void sendSeqRequest(PeerInfo peer) {
        P2PMessage<Object> msg = new P2PMessage<>();
        msg.newSeq();
        msg.setData(null);
        msg.setVersion(Versions.currentVersion);
        msg.setMethod("requestSeq");

        PeerSeqCallback callback = new PeerSeqCallback();
        callback.setHandler(messageHandler);
        callback.setPeerInfo(peer);

        logger.info("Request peer seq, peer:{}, seq:{}", peer, msg.getSeq());
        p2pEngine.asyncSendMessage(peer, msg, callback);
    }

    public void broadcastPeerInfoRequest() {
        for (PeerInfo peerInfo : peerInfos.values()) {
            sendPeerInfoRequest(peerInfo);
        }
    }

    public void sendPeerInfoRequest(PeerInfo peer) {
        sendPeerInfoRequest(peer, 0);
    }

    public void sendPeerInfoRequest(PeerInfo peer, int seq) {

        P2PMessage<Object> msg = new P2PMessage<>();

        if (seq == 0) {
            msg.newSeq();
        } else {
            msg.setSeq(seq);
        }

        msg.setData(null);
        msg.setVersion(Versions.currentVersion);
        msg.setMethod("requestPeerInfo");

        PeerInfoCallback callback = new PeerInfoCallback();
        callback.setHandler(messageHandler);
        callback.setPeerInfo(peer);

        logger.info("Request peer info, peer:{}, seq:{}", peer, msg.getSeq());
        p2pEngine.asyncSendMessage(peer, msg, callback);
    }

    public PeerSeqMessageData handleRequestSeq() {
        PeerSeqMessageData data = new PeerSeqMessageData();
        data.setSeq(seq);
        return data;
    }

    public void handleSeq(PeerInfo peer, P2PMessage msg) {
        logger.info("Receive peer seq from peer:{}", peer);
        notePeerActive(peer);

        PeerSeqMessageData data = (PeerSeqMessageData) msg.getData();
        if (data != null && msg.getMethod().equals("seq")) {
            int currentSeq = data.getSeq();
            if (hasPeerChanged(peer.getNode(), currentSeq)) {

                sendPeerInfoRequest(peer, msg.getSeq() + 1);
            }
        } else {
            logger.warn("Receive unrecognized seq message from peer:" + peer);
        }
    }

    public PeerInfoMessageData handleRequestPeerInfo() {
        Set<ResourceInfo> activeResourceSet = new HashSet<>();
        for (ResourceInfo activeResource : activeResources.values()) {
            activeResourceSet.add(activeResource);
        }

        logger.info("Receive request peer info");
        PeerInfoMessageData data = new PeerInfoMessageData();
        data.setSeq(seq);
        data.setResources(activeResourceSet);

        logger.info("Respond peerInfo to peer, resource:" + activeResources);
        return data;
    }

    public void handlePeerInfo(PeerInfo peer, P2PMessage msg) {
        logger.info("Receive peer info from {}", peer);
        notePeerActive(peer);

        PeerInfoMessageData data = (PeerInfoMessageData) msg.getData();
        if (data != null && msg.getMethod().equals("peerInfo")) {
            int currentSeq = data.getSeq();
            if (hasPeerChanged(peer.getNode(), currentSeq)) {
                // compare and update
                Set<ResourceInfo> currentResources = data.getResources();
                logger.info(
                        "Update peerInfo from {}, seq:{}, resource:{}",
                        peer,
                        currentSeq,
                        currentResources);

                peer.setResources(currentSeq, currentResources);
            } else {
                logger.info("Peer info not changed, seq:{}", currentSeq);
            }
        } else {
            logger.warn("Receive unrecognized seq message from peer:" + peer);
        }
    }

    public void setP2pEngine(P2PMessageEngine p2pEngine) {
        this.p2pEngine = p2pEngine;
    }

    public void setPeerInfos(Map<Node, PeerInfo> peerInfos) {
        this.peerInfos = peerInfos;
    }

    public synchronized PeerResources getActivePeerResources() {
        Set<PeerInfo> activeInfos = new HashSet<>();
        for (PeerInfo peerInfo : peerInfos.values()) {
            if (!peerInfo.isTimeout(peerActiveTimeout)) {
                activeInfos.add(peerInfo);
            }
        }

        // Add myself Resource Info
        Set<ResourceInfo> resourceInfos = new HashSet<>();

        return new PeerResources(activeInfos);
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

    public Map<String, ResourceInfo> getActiveResourceInfos() {
        return activeResources;
    }

    public void setActiveResourceInfos(Map<String, ResourceInfo> activeResources) {
        if (!isEqualInfos(this.activeResources, activeResources)) {
            this.newSeq();
            this.activeResources = activeResources;
            logger.info(
                    "Update active resources newSeq:{}, resource:{}", seq, this.activeResources);
        }
    }

    public void setNetworkManager(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    public void syncWithPeerNetworks() {
        // Update peers' resource into networks
        PeerResources activePeers = this.getActivePeerResources();
        networkManager.updateActivePeerNetwork(activePeers);

        // Log all active resources
        Set<String> activeResources = networkManager.getAllNetworkStubResourceName(false);
        logger.info("Current active resource:" + activeResources);

        // Update active resource back to peerManager
        Map<String, ResourceInfo> activeLocalResources =
                networkManager.getAllNetworkStubResourceInfo(true);
        logger.info("Current active local resources:" + activeLocalResources);
        this.setActiveResourceInfos(activeLocalResources);
    }

    private void workLoop() {
        try {
            // addMockResources();
            // syncWithPeerNetworks();
            broadcastSeqRequest();
        } catch (Exception e) {
            logger.warn("workloop exception: {}", e);
        }
    }

    public void addMockResources() {
        try {
            Thread.sleep(10);
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
