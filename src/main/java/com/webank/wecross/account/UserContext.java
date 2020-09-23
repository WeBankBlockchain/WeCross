package com.webank.wecross.account;

public class UserContext {
    private ThreadLocal<JwtToken> token = new ThreadLocal<>();

    public JwtToken getToken() {
        return token.get();
    }

    public void setToken(JwtToken token) {
        this.token.set(token);
    }
}
