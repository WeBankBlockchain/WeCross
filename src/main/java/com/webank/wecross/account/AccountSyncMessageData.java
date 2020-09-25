package com.webank.wecross.account;

import java.util.Collection;
import java.util.HashSet;
import lombok.Data;

@Data
public class AccountSyncMessageData {
    private int seq = 0;
    private Collection<UAProofInfo> uaProofInfos = new HashSet<>();
}
