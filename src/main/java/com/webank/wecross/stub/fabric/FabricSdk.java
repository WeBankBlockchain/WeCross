package com.webank.wecross.stub.fabric;

import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;

public class FabricSdk {

    private HFClient hfClient = null;
    private Channel channel = null;

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
}
