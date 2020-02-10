package com.webank.wecross.stub.fabric;

import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;

public class FabricConn {
    @Override
    public String toString() {
        return "FabricConn [hfClient="
                + hfClient
                + ", channel="
                + channel
                + ", name="
                + name
                + ", type="
                + type
                + ", chainCodeName="
                + chainCodeName
                + ", chainLanguage="
                + chainLanguage
                + ", chaincodeID="
                + chaincodeID
                + ", chainCodeType="
                + chainCodeType
                + "]";
    }

    private HFClient hfClient = null;
    private Channel channel = null;

    private String name;
    private String type;
    private String chainCodeName;
    private String chainLanguage;
    private ChaincodeID chaincodeID;
    private long proposalWaitTime;

    private org.hyperledger.fabric.sdk.TransactionRequest.Type chainCodeType;

    public org.hyperledger.fabric.sdk.TransactionRequest.Type getChainCodeType() {
        return chainCodeType;
    }

    public void setChainCodeType(org.hyperledger.fabric.sdk.TransactionRequest.Type chainCodeType) {
        this.chainCodeType = chainCodeType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getChainCodeName() {
        return chainCodeName;
    }

    public void setChainCodeName(String chainCodeName) {
        this.chainCodeName = chainCodeName;
    }

    public String getChainLanguage() {
        return chainLanguage;
    }

    public void setChainLanguage(String chainLanguage) {
        this.chainLanguage = chainLanguage;
    }

    public HFClient getHfClient() {
        return hfClient;
    }

    public void setHfClient(HFClient hfClient) {
        this.hfClient = hfClient;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public ChaincodeID getChaincodeID() {
        return chaincodeID;
    }

    public void setChaincodeID(ChaincodeID chaincodeID) {
        this.chaincodeID = chaincodeID;
    }

    public long getProposalWaitTime() {
        return proposalWaitTime;
    }

    public void setProposalWaitTime(long proposalWaitTime) {
        this.proposalWaitTime = proposalWaitTime;
    }
}
