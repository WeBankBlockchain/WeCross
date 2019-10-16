package com.webank.wecross.p2p;

import org.springframework.core.ParameterizedTypeReference;

public class P2PMessageCallback<T> {
    private ParameterizedTypeReference engineCallbackMessageClassType;
    protected int status;
    private String message;
    protected T data;

    public void execute() {
        this.onResponse(status, message, data);
    }

    public void onResponse(int status, String message, T data) {}

    public void setStatus(int status) {
        this.status = status;
    }

    public void setData(T msg) {
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
}
