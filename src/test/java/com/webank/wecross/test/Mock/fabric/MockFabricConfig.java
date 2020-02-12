package com.webank.wecross.test.Mock.fabric;

import com.webank.wecross.stub.fabric.FabricConfig;
import com.webank.wecross.stub.fabric.FabricPeerConfig;
import java.util.LinkedList;
import java.util.List;

public class MockFabricConfig extends FabricConfig {

    public String getChannelName() {
        return "Mock fabric channel";
    }

    public String getOrgName() {
        return "Mock fabric org";
    }

    public String getMspId() {
        return "Mock fabric msp";
    }

    public String getOrgUserName() {
        return "Mock fabric user";
    }

    public String getOrgUserKeyFile() {
        return "Mock fabric OrgUserKeyFile";
    }

    public String getOrgUserCertFile() {
        return "Mock fabric OrgUserCertFile";
    }

    public String getOrdererAddress() {
        return "grpcs://127.0.0.1:8888";
    }

    public String getOrdererTlsCaFile() {
        return "Mock fabric OrdererTlsCaFile";
    }

    public List<FabricPeerConfig> getPeerConfigs() {
        return new LinkedList<>();
    }
}
