package com.webank.wecross.jdchain.config;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDChainService {
    private Logger logger = LoggerFactory.getLogger(JDChainService.class);
    private String privateKey;
    private String publicKey;
    private String passWord;
    private List<String> connectionsStr = new ArrayList<>();

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

    public List<String> getConnectionsStr() {
        return connectionsStr;
    }

    public void setConnectionsStr(List<String> connectionsStr) {
        this.connectionsStr = connectionsStr;
    }
}
