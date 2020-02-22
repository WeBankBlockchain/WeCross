package com.webank.wecross.peer;

import com.webank.wecross.p2p.netty.common.Node;
import com.webank.wecross.resource.ResourceInfo;
import java.util.HashSet;
import java.util.Set;

public class Peer {
	Node node;
    private int seq = 1;
    private Set<ResourceInfo> resourceInfos = new HashSet<>();
    private long lastActiveTimestamp = System.currentTimeMillis();

    public Peer(Node node) {
        this.node = node;
    }
    
    public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
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

    public Set<ResourceInfo> getResourceInfos() {
        return resourceInfos;
    }

    public void setResourceInfos(Set<ResourceInfo> resourceInfos) {
        this.resourceInfos = resourceInfos;
    }

    @Override
    public String toString() {
        return this.node.toString();
    }
}
