package com.webank.wecross.account;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Date;
import java.util.Objects;

public class JwtToken {
    public static final String TOKEN_PREFIX = "Bearer ";

    private String tokenStr;
    private DecodedJWT jwt;

    public JwtToken(String tokenStr) {
        this.tokenStr = tokenStr.replaceAll(JwtToken.TOKEN_PREFIX, "").replaceAll(" ", "");
    }

    public String getIssuer() {
        prepareJwtCache();

        return jwt.getIssuer();
    }

    public String getAudience() {
        prepareJwtCache();

        return jwt.getAudience().get(0);
    }

    public Date getExpiresAt() {
        prepareJwtCache();

        return jwt.getExpiresAt();
    }

    public String getTokenStr() {
        return tokenStr;
    }

    public String getTokenStrWithPrefix() {
        return TOKEN_PREFIX + getTokenStr();
    }

    private void prepareJwtCache() {
        if (jwt == null) {
            jwt = JWT.decode(tokenStr);
        }
    }

    public boolean hasExpired() {
        return getExpiresAt().before(new Date());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JwtToken)) return false;
        JwtToken jwtToken = (JwtToken) o;
        return getTokenStr().equals(jwtToken.getTokenStr());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTokenStr());
    }
}
