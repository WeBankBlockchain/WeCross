package com.webank.wecross.peer;

import static com.webank.wecross.resource.ResourceInfo.isEqualInfos;

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
import com.webank.wecross.zone.ZoneManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeerManager {
    Logger logger = LoggerFactory.getLogger(PeerManager.class);

    private ZoneManager zoneManager;
    private Map<Node, PeerInfo> peerInfos = new HashMap<Node, PeerInfo>(); // peer
    private int seq = 1; // Seq of the host
    private long peerActiveTimeout;

    private Map<String, ResourceInfo> activeResources = new HashMap<>();

    public void newSeq() {
        this.seq = SeqUtils.newSeq();
    }

    public int getSeq() {
        return seq;
    }

    public ZoneManager getZoneManager() {
        return this.zoneManager;
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

    public synchronized PeerInfo addPeerInfo(Node node) {
    	PeerInfo peerInfo = new PeerInfo(node);
        peerInfos.put(node, peerInfo);
        
        return peerInfo;
    }

    public synchronized void removePeerInfo(Node node) {
    	PeerInfo peerInfo = peerInfos.get(node);
    	if(peerInfo == null) {
    		logger.error("Peer not exists, bug?");
    		return;
    	}
    	
    	zoneManager.removeRemoteResources(peerInfo, peerInfo.getResourceInfos());
        peerInfos.remove(node);
        
    }

    public synchronized void clearPeerInfos() {
        peerInfos.clear();
    }

    public synchronized void notePeerActive(PeerInfo peer) {
        peer.noteAlive();
    }

    public synchronized boolean hasPeerChanged(Node node, int currentSeq) {
        if (!peerInfos.containsKey(node)) {
            return true;
        }
        return peerInfos.get(node).getSeq() != currentSeq;
    }

    public PeerSeqMessageData handleRequestSeq() {
        PeerSeqMessageData data = new PeerSeqMessageData();
        data.setSeq(seq);
        return data;
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

        return new PeerResources(activeInfos);
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

    public void setZoneManager(ZoneManager networkManager) {
        this.zoneManager = networkManager;
    }
}
