package com.webank.wecross.p2p;

import org.springframework.core.ParameterizedTypeReference;

public class P2PMessageCallback<T> {
    private ParameterizedTypeReference engineCallbackMessageClassType;
    protected int status;
    protected T msg;

    public void execute() {
        this.onResponse(status, msg);
    }

    public void onResponse(int status, T msg) {}

    public void setStatus(int status) {
        this.status = status;
    }

    public void setMsg(T msg) {
        this.msg = msg;
    }

    public ParameterizedTypeReference getEngineCallbackMessageClassType() {
        return engineCallbackMessageClassType;
    }

    public void setEngineCallbackMessageClassType(
            ParameterizedTypeReference engineCallbackMessageClassType) {
        this.engineCallbackMessageClassType = engineCallbackMessageClassType;
    }
}
