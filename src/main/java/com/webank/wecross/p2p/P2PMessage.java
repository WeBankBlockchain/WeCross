package com.webank.wecross.p2p;

import java.security.SecureRandom;

public class P2PMessage<T> {
    static final int SEQ_BOUND = Integer.MAX_VALUE - 1;
    private String version;
    private String type;
    private int seq;
    private T data;

    public void newSeq() {
        SecureRandom rand = new SecureRandom();
        this.seq = rand.nextInt(SEQ_BOUND);
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
