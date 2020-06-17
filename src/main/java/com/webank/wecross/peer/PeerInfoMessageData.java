package com.webank.wecross.peer;

import com.webank.wecross.zone.ChainInfo;
import java.util.Map;

public class PeerInfoMessageData {
    private int seq;
    private Map<String, ChainInfo> chainInfos;

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public Map<String, ChainInfo> getChainInfos() {
        return chainInfos;
    }

    public void setChainInfos(Map<String, ChainInfo> chainInfos) {
        this.chainInfos = chainInfos;
    }
}
