package com.webank.wecross.host;

import com.webank.wecross.core.SeqUtils;
import com.webank.wecross.network.NetworkManager;
import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.P2PMessageData;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.peer.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeerManager {
    Logger logger = LoggerFactory.getLogger(PeerManager.class);

    @Resource(name = "newRestfulP2PMessageEngine")
    private P2PMessageEngine p2pEngine;

    @Resource(name = "initPeers")
    private Map<String, Peer> peers; // url -> peer

    private int seq = 1; // Seq of the host

    @Resource(name = "newSyncPeerMessageHandler")
    public SyncPeerMessageHandler messageHandler;

    @Resource NetworkManager networkManager;

    public void start() {
        newSeq();
        broadcastSeqRequest();
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
        PeerRequestPeerInfoMessageData data = new PeerRequestPeerInfoMessageData();
        P2PMessage<PeerRequestPeerInfoMessageData> msg = new P2PMessage<>();
        msg.newSeq();
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
        PeerSeqMessageData data = (PeerSeqMessageData) msg.getData();
        if (data != null && data.getMethod().equals("seq")) {
            int currentSeq = data.getDataSeq();
            if (hasPeerChanged(peer.getUrl(), currentSeq)) {
                logger.info("Request peerInfo from {}", peer);

                sendPeerInfoRequest(peer);
            }
        } else {
            logger.warn("Receive unrecognized seq message from peer:" + peer);
        }
    }

    public PeerInfoMessageData handleRequestPeerInfo() {
        PeerInfoMessageData data = new PeerInfoMessageData();
        data.setDataSeq(seq);

        Set<String> resources = networkManager.getAllNetworkStubResourceName();
        for (String resource : resources) {
            data.addResource(resource);
        }
        return data;
    }

    public void handlePeerInfo(Peer peer, P2PMessage msg) {
        logger.info("Receive peer info from {}", peer);
        PeerInfoMessageData data = (PeerInfoMessageData) msg.getData();
        if (data != null && data.getMethod().equals("peerInfo")) {
            int currentSeq = data.getDataSeq();
            if (hasPeerChanged(peer.getUrl(), currentSeq)) {
                // compare and update
                Set<String> currentResources = data.getDataResources();
                logger.info(
                        "Update peerInfo url{}, seq:{}, resource:{}",
                        peer,
                        currentSeq,
                        currentResources);

                Peer peerRecord = getPeer(peer.getUrl());
                if (peerRecord == null) {
                    peer.setResources(currentSeq, currentResources);
                    updatePeer(peer);
                } else {
                    // TODO compare and update in networkManager
                    peerRecord.setResources(currentSeq, currentResources);
                }
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
}
