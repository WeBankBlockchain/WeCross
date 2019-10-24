package com.webank.wecross.p2p;

import com.webank.wecross.core.SeqUtils;

public class P2PMessage<T> {
    private String version;
    private int seq;
    private String method;
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

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
