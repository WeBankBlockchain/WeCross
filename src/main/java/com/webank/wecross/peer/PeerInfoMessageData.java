package com.webank.wecross.peer;

import com.webank.wecross.stub.ResourceInfo;
import java.util.HashMap;
import java.util.Map;

public class PeerInfoMessageData {
    private int seq;
    private Map<String, ResourceInfo> resources = new HashMap<String, ResourceInfo>();

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public Map<String, ResourceInfo> getResources() {
        return resources;
    }

    public void setResources(Map<String, ResourceInfo> resources) {
        this.resources = resources;
    }

    public void addResource(String path, ResourceInfo resource) {
        resources.put(path, resource);
    }
}
