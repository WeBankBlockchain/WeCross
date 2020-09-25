package com.webank.wecross.account;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

@Data
@Builder
public class UAProofInfo {
    @Tolerate
    public UAProofInfo() {}

    private String chainAccountID;
    private String uaID;
    private String type;
    private String uaProof;
}
