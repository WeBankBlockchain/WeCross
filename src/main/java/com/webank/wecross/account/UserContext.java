package com.webank.wecross.account;

public class UserContext {
    private JwtToken token;

    public JwtToken getToken() {
        return token;
    }

    public void setToken(JwtToken token) {
        this.token = token;
    }
}
