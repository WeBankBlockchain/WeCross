package com.webank.wecross.p2p;

import com.webank.wecross.core.SeqUtils;

public class P2PMessage<T> {
    private String version;
    private String type;
    private int seq;
    private T data;

    public void newSeq() {

        this.seq = SeqUtils.newSeq();
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String toUri() {
        // Only for RestfulP2PMessageEngine
        if (data != null && ((P2PMessageData) data).getMethod() != null) {
            return "/" + type + "/" + ((P2PMessageData) data).getMethod();
        }
        return "/" + type;
    }
}
