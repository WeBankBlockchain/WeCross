package com.webank.wecross.p2p.netty.common;

import java.util.Objects;

public class Host {

    private String host;
    private Integer port;

    public Host() {}

    public Host(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    public Host(String host, Integer port, String nodeId) {
        this.host = host;
        this.port = port;
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
        Host peerHost = (Host) o;
        return Objects.equals(host, peerHost.host) && Objects.equals(port, peerHost.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }

    @Override
    public String toString() {
        return "[" + "host='" + host + '\'' + ", port=" + port + ']';
    }
}
