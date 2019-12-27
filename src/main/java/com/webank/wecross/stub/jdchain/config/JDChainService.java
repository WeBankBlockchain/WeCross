package com.webank.wecross.stub.jdchain.config;

public class JDChainService {

    private String privateKey;
    private String publicKey;
    private String password;
    private String connectionsStr;

    public JDChainService(
            String privateKey, String publicKey, String password, String connectionsStr) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.password = password;
        this.connectionsStr = connectionsStr;
    }

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConnectionsStr() {
        return connectionsStr;
    }

    public void setConnectionsStr(String connectionsStr) {
        this.connectionsStr = connectionsStr;
    }
}
