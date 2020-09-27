package com.webank.wecross.account;

import com.webank.wecross.account.uaproof.UAProofInfo;
import java.util.Collection;
import java.util.HashSet;

public class AccountSyncMessageData {
    private int seq = 0;
    private Collection<UAProofInfo> uaProofInfos = new HashSet<>();

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public Collection<UAProofInfo> getUaProofInfos() {
        return uaProofInfos;
    }

    public void setUaProofInfos(Collection<UAProofInfo> uaProofInfos) {
        this.uaProofInfos = uaProofInfos;
    }
}
