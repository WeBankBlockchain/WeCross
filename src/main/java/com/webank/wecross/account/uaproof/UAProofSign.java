package com.webank.wecross.account.uaproof;

import java.nio.charset.StandardCharsets;
import lombok.Data;

@Data
public class UAProofSign {
    private byte[] signBytes;
    private String signer;
    private String signee;
    private String timestamp;

    public byte[] getMessage() {
        String fullSignString = signee + "," + timestamp;
        return fullSignString.getBytes(StandardCharsets.UTF_8);
    }
}
