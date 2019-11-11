package com.webank.wecross.peer;

import com.webank.wecross.p2p.netty.common.Peer;
import com.webank.wecross.resource.ResourceInfo;

import java.util.HashSet;
import java.util.Set;

public class PeerInfo {
    Peer peer;
    private int seq = 1;
    private Set<ResourceInfo> resourceInfos = new HashSet<>();
    private long lastActiveTimestamp = System.currentTimeMillis();

    public PeerInfo(Peer peer) {
        this.peer = peer;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }


    public synchronized void setResources(int seq, Set<ResourceInfo> resourceInfos) {
        this.setSeq(seq);
        this.resourceInfos = resourceInfos;
    }

    public void noteAlive() {
        this.lastActiveTimestamp = System.currentTimeMillis();
    }

    public boolean isTimeout(long timeout) {
        return (System.currentTimeMillis() - lastActiveTimestamp) > timeout;
    }

    public long getLastActiveTimestamp() {
        return lastActiveTimestamp;
    }

    public Peer getPeer() {
        assert (this.peer != null);
        return this.peer;
    }

    public void setPeer(Peer peer) {
        this.peer = peer;
    }

    public Set<ResourceInfo> getResourceInfos() {
        return resourceInfos;
    }

    public void setResourceInfos(Set<ResourceInfo> resourceInfos) {
        this.resourceInfos = resourceInfos;
    }
}
