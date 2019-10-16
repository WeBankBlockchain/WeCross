package com.webank.wecross.p2p.peer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.webank.wecross.p2p.P2PMessageData;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PeerSeqMessageData implements P2PMessageData {
    public PeerSeqMessageData() {
        this.data = new Data();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public class Data {
        public int seq;
    }

    @JsonIgnore
    public void setDataSeq(int seq) {
        this.data.seq = seq;
    }

    @JsonIgnore
    public int getDataSeq() {
        return this.data.seq;
    }

    private String version = "0.1";
    private String method = "seq";
    private Data data;

    @Override
    public String getMethod() {
        return method;
    }
}
