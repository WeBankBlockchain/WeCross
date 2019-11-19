package com.webank.wecross.stub.fabric.config;

public class FabricPeerConfig {

    private String peerAddress;
    private String peerTlsCaFile;

    public String getPeerAddress() {
        return peerAddress;
    }

    public void setPeerAddress(String peerAddress) {
        this.peerAddress = peerAddress;
    }

    public String getPeerTlsCaFile() {
        return peerTlsCaFile;
    }

    public void setPeerTlsCaFile(String peerTlsCaFile) {
        this.peerTlsCaFile = peerTlsCaFile;
    }
};
