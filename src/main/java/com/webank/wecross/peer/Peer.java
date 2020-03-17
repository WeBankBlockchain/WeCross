package com.webank.wecross.peer;

import com.webank.wecross.p2p.netty.common.Node;
import com.webank.wecross.stub.ResourceInfo;
import java.util.HashMap;
import java.util.Map;

public class Peer {
    Node node;
    private int seq = 0;
    private Map<String, ResourceInfo> resourceInfos = new HashMap<String, ResourceInfo>();
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

    public synchronized void setResources(int seq, Map<String, ResourceInfo> resourceInfos) {
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

    public Map<String, ResourceInfo> getResourceInfos() {
        return resourceInfos;
    }

    public void setResourceInfos(Map<String, ResourceInfo> resourceInfos) {
        this.resourceInfos = resourceInfos;
    }

    @Override
    public String toString() {
        return this.node.toString();
    }
}
