package com.webank.wecross.account;

public class UserContext {
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token; // token.replaceAll(JwtToken.TOKEN_PREFIX, "").replaceAll(" ", "");
    }
}
