package com.webank.wecross.p2p;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.webank.wecross.host.Peer;
import org.springframework.core.ParameterizedTypeReference;

public class P2PMessageCallback<T> {
    private ParameterizedTypeReference engineCallbackMessageClassType;
    protected int status;
    protected String message;
    protected P2PMessage<T> data;

    private Peer peer;

    public void execute() {
        this.onResponse(status, message, data);
    }

    public void onResponse(int status, String message, P2PMessage msg) {}

    public void setStatus(int status) {
        this.status = status;
    }

    @JsonIgnore
    public int getStatus() {
        return this.status;
    }

    public void setData(P2PMessage msg) {
        this.data = msg;
    }

    public ParameterizedTypeReference getEngineCallbackMessageClassType() {
        return engineCallbackMessageClassType;
    }

    public void setEngineCallbackMessageClassType(
            ParameterizedTypeReference engineCallbackMessageClassType) {
        this.engineCallbackMessageClassType = engineCallbackMessageClassType;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @JsonIgnore
    public String getMessage() {
        return this.message;
    }

    @JsonIgnore
    public Peer getPeer() {
        return peer;
    }

    @JsonIgnore
    public void setPeer(Peer peer) {
        this.peer = peer;
    }
}
