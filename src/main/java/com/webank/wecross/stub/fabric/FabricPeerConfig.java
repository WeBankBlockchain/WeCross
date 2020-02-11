package com.webank.wecross.stub.fabric;

public class FabricPeerConfig {

    @Override
    public String toString() {
        return "FabricPeerConfig [peerAddress="
                + peerAddress
                + ", peerTlsCaFile="
                + peerTlsCaFile
                + "]";
    }

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
