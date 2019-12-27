package com.webank.wecross.p2p.netty.common;

import java.util.Objects;

public class Peer {

    String nodeID;

    public Peer() {}

    public Peer(String nodeID) {
        this.nodeID = nodeID;
    }

    public String getNodeID() {
        return nodeID;
    }

    public void setNodeID(String nodeID) {
        this.nodeID = nodeID;
    }

    @Override
    public String toString() {
        return "Node{" + "nodeID='" + nodeID + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Peer peer = (Peer) o;
        return Objects.equals(nodeID, peer.nodeID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeID);
    }
}
