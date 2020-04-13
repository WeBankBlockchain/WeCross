package com.webank.wecross.peer;

import static com.webank.wecross.stub.ResourceInfo.isEqualInfos;

import com.webank.wecross.p2p.netty.common.Node;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.utils.core.SeqUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeerManager {
    Logger logger = LoggerFactory.getLogger(PeerManager.class);

    private Map<Node, Peer> peerInfos = new ConcurrentHashMap<Node, Peer>(); // peer
    private int seq = 1; // Seq of the host
    private long peerActiveTimeout;

    private Map<String, ResourceInfo> activeResources = new HashMap<>();

    public void newSeq() {
        this.seq = SeqUtils.newSeq();
    }

    public int getSeq() {
        return seq;
    }

    public int peerSize() {
        return peerInfos.size();
    }

    public Map<Node, Peer> getPeerInfos() {
        return peerInfos;
    }

    public synchronized Peer getPeerInfo(Node node) {
        if (peerInfos.containsKey(node)) {
            return peerInfos.get(node);
        } else {
            return null;
        }
    }

    public synchronized Peer addPeerInfo(Node node) {
        Peer peerInfo = new Peer(node);
        if (peerInfos.containsKey(node)) {
            logger.error("");
        }

        peerInfos.put(node, peerInfo);

        return peerInfo;
    }

    public synchronized void removePeerInfo(Node node) {
        Peer peerInfo = peerInfos.get(node);
        if (peerInfo == null) {
            logger.error("Peer not exists, bug?");
            return;
        }

        peerInfos.remove(node);
    }

    public synchronized void clearPeerInfos() {
        peerInfos.clear();
    }

    public synchronized void notePeerActive(Peer peer) {
        peer.noteAlive();
    }

    public synchronized boolean hasPeerChanged(Node node, int currentSeq) {
        if (!peerInfos.containsKey(node)) {
            return true;
        }
        return peerInfos.get(node).getSeq() != currentSeq;
    }

    public void setPeerInfos(Map<Node, Peer> peerInfos) {
        this.peerInfos = peerInfos;
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
}
