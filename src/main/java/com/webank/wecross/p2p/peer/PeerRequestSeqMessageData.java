package com.webank.wecross.p2p.peer;

import com.webank.wecross.p2p.P2PMessageData;

public class PeerRequestSeqMessageData implements P2PMessageData {
    private String version = "0.1";
    private String method = "requestSeq";
    private Object data;

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

    public void setData(Object data) {
        this.data = data;
    }
}
