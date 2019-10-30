package com.webank.wecross.peer;

import java.util.HashSet;
import java.util.Set;

public class PeerInfoMessageData {

    private int seq;
    private Set<String> resources = new HashSet<>();

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public Set<String> getResources() {
        return resources;
    }

    public void setResources(Set<String> resources) {
        this.resources = resources;
    }

    public void addResource(String resource) {
        resources.add(resource);
    }
}
