package com.webank.wecross.p2p.peer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.webank.wecross.p2p.P2PMessageData;
import java.util.HashSet;
import java.util.Set;

public class PeerInfoMessageData implements P2PMessageData {
    private String version = "0.1";
    private String method = "peerInfo";
    private Data data = new Data();

    public void addResource(String resource) {
        data.resources.add(resource);
    }

    public String getVersion() {
        return version;
    }

    public class Data {
        public int seq;
        public Set<String> resources = new HashSet<>();
    }

    @Override
    public String getMethod() {
        return method;
    }

    @JsonIgnore
    public void setDataSeq(int seq) {
        this.data.seq = seq;
    }

    @JsonIgnore
    public int getDataSeq() {
        return this.data.seq;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
}
