package com.webank.wecross.p2p.stub;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.webank.wecross.p2p.P2PMessageData;

public class StubChainStateMessageData implements P2PMessageData {
    private String method = "chainSate";
    private String version = "0.1";
    private Data data;

    public class Data {
        public BlockHeaderData header;
    }

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

    public void setData(Data data) {
        this.data = data;
    }

    public Data getData() {
        return data;
    }

    public void setDataHeader(BlockHeaderData header) {
        data.header = header;
    }

    @JsonIgnore
    public BlockHeaderData getDataHeader() {
        return data.header;
    }

    @JsonIgnore
    public int getNumber() {
        return data.header.number;
    }

    @JsonIgnore
    public String getHash() {
        return data.header.hash;
    }
}
