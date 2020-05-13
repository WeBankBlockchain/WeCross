package com.webank.wecross.network.p2p.netty.common;

import java.util.Objects;

public class Node {
    private String nodeID;
    private String host;
    private Integer port;

    public Node() {}

    public Node(String nodeID, String host, Integer port) {
        this.setNodeID(nodeID);
        this.host = host;
        this.port = port;
    }

    public String getIPPort() {
        return host + ":" + String.valueOf(port);
    }

    public String getNodeID() {
        return nodeID;
    }

    public void setNodeID(String nodeID) {
        this.nodeID = nodeID;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Node peerHost = (Node) o;

        return Objects.equals(nodeID, peerHost.nodeID)
                && Objects.equals(host, peerHost.host)
                && Objects.equals(port, peerHost.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeID, host, port);
    }

    @Override
    public String toString() {
        return "[" + "nodeID=" + nodeID + ", host='" + host + '\'' + ", port=" + port + ']';
    }
}
