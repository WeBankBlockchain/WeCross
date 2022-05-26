package com.webank.wecross.account;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChainAccountDetails {
    private String username; // ua
    private Integer keyID;
    private String identity;
    private String type;

    private Boolean isDefault;
    private String fabricDefault;

    private String pubKey;
    private String secKey;
    private String ext0;
    private String ext1;
    private String ext2;
    private String ext3;

    public Map<String, Object> toProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("username", username);
        properties.put("keyID", keyID);
        properties.put("type", type);
        properties.put("isDefault", isDefault);
        properties.put("fabricDefault", fabricDefault);
        properties.put("pubKey", pubKey);
        properties.put("secKey", secKey);
        properties.put("ext0", ext0);
        properties.put("ext1", ext1);
        properties.put("ext2", ext2);
        properties.put("ext3", ext3);
        return properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChainAccountDetails)) return false;
        ChainAccountDetails that = (ChainAccountDetails) o;
        return getKeyID().equals(that.getKeyID()) && getType().equals(that.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKeyID(), getType());
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getKeyID() {
        return keyID;
    }

    public void setKeyID(Integer keyID) {
        this.keyID = keyID;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonGetter("isDefault")
    public Boolean getIsDefault() {
        return isDefault;
    }

    @JsonSetter("isDefault")
    public void setIsDefault(Boolean aDefault) {
        isDefault = aDefault;
    }

    @JsonGetter("fabricDefault")
    public String getFabricDefault(){
        return fabricDefault;
    }

    @JsonSetter("fabricDefault")
    public void setFabricDefault(String chainName){
        fabricDefault = chainName;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public String getSecKey() {
        return secKey;
    }

    public void setSecKey(String secKey) {
        this.secKey = secKey;
    }

    public String getExt0() {
        return ext0;
    }

    public void setExt0(String ext0) {
        this.ext0 = ext0;
    }

    public String getExt1() {
        return ext1;
    }

    public void setExt1(String ext1) {
        this.ext1 = ext1;
    }

    public String getExt2() {
        return ext2;
    }

    public void setExt2(String ext2) {
        this.ext2 = ext2;
    }

    public String getExt3() {
        return ext3;
    }

    public void setExt3(String ext3) {
        this.ext3 = ext3;
    }
}
