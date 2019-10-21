package com.webank.wecross.p2p.stub;

import com.webank.wecross.p2p.P2PMessageData;

public class StubRequestChainStateMessageData implements P2PMessageData {
    private String method = "requestChainState";
    private String version = "0.1";
    private Object data = null; // always empty

    @Override
    public String getMethod() {
        return method;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Object getData() {
        return data;
    }
}
