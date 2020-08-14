package com.webank.wecross.peer;

import com.webank.wecross.network.p2p.netty.common.Node;
import com.webank.wecross.zone.ChainInfo;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Peer {
    Node node;
    private int seq = 0;
    private Map<String, ChainInfo> chainInfos = new HashMap<String, ChainInfo>();
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

    public synchronized void setChainInfos(int seq, Map<String, ChainInfo> chainInfos) {
        this.setSeq(seq);
        this.chainInfos = chainInfos;
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

    public Map<String, ChainInfo> getChainInfos() {
        return chainInfos;
    }

    public void setChainInfos(Map<String, ChainInfo> chainInfos) {
        this.chainInfos = chainInfos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Peer)) return false;
        Peer peer = (Peer) o;
        return getNode().equals(peer.getNode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNode());
    }

    @Override
    public String toString() {
        return "Peer{" + "node=" + node + ", seq=" + seq + '}';
    }
}
