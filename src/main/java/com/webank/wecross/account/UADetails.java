package com.webank.wecross.account;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.Map;

public class UADetails {
    private String username;
    private String uaID;
    private String pubKey;
    private String password;
    private String secKey;
    private String role;
    private String[] allowChainPaths;
    private Long version;

    private boolean isAdmin;

    private Map<String, Map<Integer, ChainAccountDetails>> type2ChainAccountDetails;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUaID() {
        return uaID;
    }

    public void setUaID(String uaID) {
        this.uaID = uaID;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSecKey() {
        return secKey;
    }

    public void setSecKey(String secKey) {
        this.secKey = secKey;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @JsonGetter("isAdmin")
    public boolean isAdmin() {
        return isAdmin;
    }

    @JsonSetter("isAdmin")
    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public Map<String, Map<Integer, ChainAccountDetails>> getType2ChainAccountDetails() {
        return type2ChainAccountDetails;
    }

    public void setType2ChainAccountDetails(
            Map<String, Map<Integer, ChainAccountDetails>> type2ChainAccountDetails) {
        this.type2ChainAccountDetails = type2ChainAccountDetails;
    }

    public String[] getAllowChainPaths() {
        return allowChainPaths;
    }

    public void setAllowChainPaths(String[] allowChainPaths) {
        this.allowChainPaths = allowChainPaths;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
