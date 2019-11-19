package com.webank.wecross.stub.fabric.config;

import java.util.ArrayList;
import java.util.List;

public class FabricConfig {
    private String channelName;

    private String orgName;

    private String mspId;

    private String orgUserName;

    private String orgUserKeyFile;

    private String orgUserCertFile;

    private List<FabricPeerConfig> peerConfigs = new ArrayList<FabricPeerConfig>();

    private String ordererAddress;

    private String ordererTlsCaFile;

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getMspId() {
        return mspId;
    }

    public void setMspId(String mspId) {
        this.mspId = mspId;
    }

    public String getOrgUserName() {
        return orgUserName;
    }

    public void setOrgUserName(String orgUserName) {
        this.orgUserName = orgUserName;
    }

    public String getOrgUserKeyFile() {
        return orgUserKeyFile;
    }

    public void setOrgUserKeyFile(String orgUserKeyFile) {
        this.orgUserKeyFile = orgUserKeyFile;
    }

    public String getOrgUserCertFile() {
        return orgUserCertFile;
    }

    public void setOrgUserCertFile(String orgUserCertFile) {
        this.orgUserCertFile = orgUserCertFile;
    }

    public String getOrdererAddress() {
        return ordererAddress;
    }

    public void setOrdererAddress(String ordererAddress) {
        this.ordererAddress = ordererAddress;
    }

    public String getOrdererTlsCaFile() {
        return ordererTlsCaFile;
    }

    public void setOrdererTlsCaFile(String ordererTlsCaFile) {
        this.ordererTlsCaFile = ordererTlsCaFile;
    }

    public List<FabricPeerConfig> getPeerConfigs() {
        return peerConfigs;
    }

    public void setPeerConfigs(List<FabricPeerConfig> peerConfigs) {
        this.peerConfigs = peerConfigs;
    }
}
