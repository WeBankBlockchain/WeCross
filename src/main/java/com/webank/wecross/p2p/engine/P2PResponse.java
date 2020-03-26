package com.webank.wecross.p2p.engine;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.webank.wecross.p2p.P2PMessage;

public class P2PResponse<T> {
    private String version;
    private int seq;
    private int errorCode = 0;
    private String message;
    private T data;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @JsonIgnore
    public P2PMessage toP2PMessage(String method) {
        P2PMessage msg = new P2PMessage<>();
        msg.setVersion(this.getVersion());
        msg.setMethod(method);
        msg.setSeq(this.getSeq());
        msg.setData(this.getData());
        return msg;
    }
}
