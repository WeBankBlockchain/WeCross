package com.webank.wecross.restserver;

public class RPCContext {
    private ThreadLocal<String> token = new ThreadLocal<>();

    public String getToken() {
        return token.get();
    }

    public void setToken(String token) {
        this.token.set(token);
    }
}
