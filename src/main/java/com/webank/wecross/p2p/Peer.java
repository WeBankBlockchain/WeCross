package com.webank.wecross.p2p;

import java.util.HashSet;
import java.util.Set;

public class Peer {
    private String url;
    private String name;

    public Peer() {}

    public Peer(String url, String name) {
        this.url = url;
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return "(name:" + name + ", url:" + url + ")";
    }
}
