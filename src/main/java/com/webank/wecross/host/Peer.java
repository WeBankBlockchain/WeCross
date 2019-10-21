package com.webank.wecross.host;

import java.util.HashSet;
import java.util.Set;

public class Peer {
    private String url;
    private String name;
    private int seq = 1;
    private Set<String> resources = new HashSet<>();
    private long lastActiveTimestamp = System.currentTimeMillis();

    public Peer() {}

    public Peer(String url, String name) {
        this.url = url;
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public Set<String> getResources() {
        return resources;
    }

    public synchronized void setResources(int seq, Set<String> resources) {
        this.setSeq(seq);
        this.resources = resources;
    }

    public void noteAlive() {
        this.lastActiveTimestamp = System.currentTimeMillis();
    }

    public boolean isTimeout(long timeout) {
        return (System.currentTimeMillis() - lastActiveTimestamp) > timeout;
    }

    @Override
    public String toString() {
        return "peer(name:" + name + ", url:" + url + ")";
    }

    public long getLastActiveTimestamp() {
        return lastActiveTimestamp;
    }
}
