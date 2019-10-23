package com.webank.wecross.host;

import com.webank.wecross.core.SeqUtils;
import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.P2PMessageData;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.peer.PeerInfoCallback;
import com.webank.wecross.p2p.peer.PeerInfoMessageData;
import com.webank.wecross.p2p.peer.PeerRequestPeerInfoMessageData;
import com.webank.wecross.p2p.peer.PeerRequestSeqMessageData;
import com.webank.wecross.p2p.peer.PeerSeqCallback;
import com.webank.wecross.p2p.peer.PeerSeqMessageData;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeerManager {
    Logger logger = LoggerFactory.getLogger(PeerManager.class);

    private P2PMessageEngine p2pEngine;

    private Map<String, Peer> peers; // url -> peer

    private int seq = 1; // Seq of the host

    private SyncPeerMessageHandler messageHandler;

    private long peerActiveTimeout;

    private Set<String> activeResources = new HashSet<>();

    public void start() {
        newSeq();
        // broadcastSeqRequest();
    }

    public void newSeq() {
        seq = this.seq = SeqUtils.newSeq();
    }

    public int getSeq() {
        return seq;
    }

    public int peerSize() {
        return peers.size();
    }

    public synchronized Peer getPeer(String url) {
        if (peers.containsKey(url)) {
            return peers.get(url);
        } else {
            return null;
        }
    }

    public synchronized void updatePeer(Peer peer) {
        peers.put(peer.getUrl(), peer);
    }

    public synchronized void removePeer(String url) {
        peers.remove(url);
    }

    public synchronized void clearPeers() {
        peers.clear();
    }

    public synchronized void notePeerActive(String url) {
        Peer peer = getPeer(url);
        if (peer == null) {
            updatePeer(new Peer(url, ""));
        }
        getPeer(url).noteAlive();
    }

    public synchronized boolean hasPeerChanged(String url, int currentSeq) {
        if (!peers.containsKey(url)) {
            return true;
        }
        return peers.get(url).getSeq() != currentSeq;
    }

    public P2PMessageData onRestfulPeerMessage(String method, P2PMessage msg) {
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
        PeerRequestSeqMessageData data = new PeerRequestSeqMessageData();
        P2PMessage<PeerRequestSeqMessageData> msg = new P2PMessage<>();
        msg.newSeq();
        msg.setData(data);
        msg.setVersion("0.1");
        msg.setType("peer");

        for (Peer peer : peers.values()) {
            PeerSeqCallback callback = new PeerSeqCallback();
            callback.setHandler(messageHandler);
            callback.setPeer(peer);
            p2pEngine.asyncSendMessage(peer, msg, callback);
        }
    }

    public void broadcastPeerInfoRequest() {
        PeerRequestPeerInfoMessageData data = new PeerRequestPeerInfoMessageData();
        P2PMessage<PeerRequestPeerInfoMessageData> msg = new P2PMessage<>();
        msg.newSeq();
        msg.setData(data);
        msg.setVersion("0.1");
        msg.setType("peer");

        for (Peer peer : peers.values()) {
            PeerInfoCallback callback = new PeerInfoCallback();
            callback.setHandler(messageHandler);
            callback.setPeer(peer);
            p2pEngine.asyncSendMessage(peer, msg, callback);
        }
    }

    public void sendPeerInfoRequest(Peer peer) {
        sendPeerInfoRequest(peer, 0);
    }

    public void sendPeerInfoRequest(Peer peer, int seq) {
        PeerRequestPeerInfoMessageData data = new PeerRequestPeerInfoMessageData();
        P2PMessage<PeerRequestPeerInfoMessageData> msg = new P2PMessage<>();

        if (seq == 0) {
            msg.newSeq();
        } else {
            msg.setSeq(seq);
        }

        msg.setData(data);
        msg.setVersion("0.1");
        msg.setType("peer");

        PeerInfoCallback callback = new PeerInfoCallback();
        callback.setHandler(messageHandler);
        callback.setPeer(peer);

        p2pEngine.asyncSendMessage(peer, msg, callback);
    }

    public PeerSeqMessageData handleRequestSeq() {
        PeerSeqMessageData data = new PeerSeqMessageData();
        data.setDataSeq(seq);
        return data;
    }

    public void handleSeq(Peer peer, P2PMessage msg) {
        logger.info("Receive peer seq from {}", peer);
        notePeerActive(peer.getUrl());

        PeerSeqMessageData data = (PeerSeqMessageData) msg.getData();
        if (data != null && data.getMethod().equals("seq")) {
            int currentSeq = data.getDataSeq();
            if (hasPeerChanged(peer.getUrl(), currentSeq)) {
                logger.info("Request peerInfo to {}", peer);
                sendPeerInfoRequest(peer, msg.getSeq() + 1);
            }
        } else {
            logger.warn("Receive unrecognized seq message from peer:" + peer);
        }
    }

    public PeerInfoMessageData handleRequestPeerInfo() {
        logger.info("Receive request peer info");
        PeerInfoMessageData data = new PeerInfoMessageData();
        data.setDataSeq(seq);

        for (String resource : activeResources) {
            data.addResource(resource);
        }
        logger.info("Respond peerInfo: " + activeResources);
        return data;
    }

    public void handlePeerInfo(Peer peer, P2PMessage msg) {
        logger.info("Receive peer info from {}", peer);
        notePeerActive(peer.getUrl());

        PeerInfoMessageData data = (PeerInfoMessageData) msg.getData();
        if (data != null && data.getMethod().equals("peerInfo")) {
            int currentSeq = data.getDataSeq();
            if (hasPeerChanged(peer.getUrl(), currentSeq)) {
                // compare and update
                Set<String> currentResources = data.getDataResources();
                logger.info(
                        "Update peerInfo peer:{}, seq:{}, resource:{}",
                        peer,
                        currentSeq,
                        currentResources);

                Peer peerRecord = getPeer(peer.getUrl());
                if (peerRecord == null) {
                    peer.setResources(currentSeq, currentResources);
                    updatePeer(peer);
                } else {
                    peerRecord.setResources(currentSeq, currentResources);
                }
            } else {
                logger.info("Peer info not changed, seq:{}", currentSeq);
            }
        } else {
            logger.warn("Receive unrecognized seq message from peer:" + peer);
        }
    }

    public Set<String> getAllPeerResource() {
        Set<String> ret = new HashSet<>();
        for (Peer peer : peers.values()) {
            if (peer.getResources() != null) {
                for (String resource : peer.getResources()) {
                    ret.add(resource);
                }
            }
        }
        return ret;
    }

    public void setP2pEngine(P2PMessageEngine p2pEngine) {
        this.p2pEngine = p2pEngine;
    }

    public void setPeers(Map<String, Peer> peers) {
        this.peers = peers;
    }

    public synchronized Set<Peer> getActivePeers() {
        Set<Peer> ret = new HashSet<>();
        for (Peer peer : peers.values()) {
            if (!peer.isTimeout(peerActiveTimeout)) {
                ret.add(peer);
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
}
