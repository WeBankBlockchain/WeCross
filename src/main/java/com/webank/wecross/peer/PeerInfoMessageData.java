package com.webank.wecross.peer;

import com.webank.wecross.resource.ResourceInfo;

import java.util.HashSet;
import java.util.Set;

public class PeerInfoMessageData {

    private int seq;
    private Set<ResourceInfo> resources = new HashSet<>();

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public Set<ResourceInfo> getResources() {
        return resources;
    }

    public void setResources(Set<ResourceInfo> resources) {
        this.resources = resources;
    }

    public void addResource(ResourceInfo resource) {
        resources.add(resource);
    }
}
