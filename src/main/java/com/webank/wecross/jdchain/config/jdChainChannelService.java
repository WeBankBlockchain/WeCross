package com.webank.wecross.jdchain.config;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class jdChainChannelService {
    private Logger logger = LoggerFactory.getLogger(jdChainChannelService.class);
    private String privateKey;
    private String publicKey;
    private String passWord;
    private List<String> allChannelConnections = new ArrayList<>();

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public List<String> getAllChannelConnections() {
        return allChannelConnections;
    }

    public void setAllChannelConnections(List<String> allChannelConnections) {
        this.allChannelConnections = allChannelConnections;
    }
}
