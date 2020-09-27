package com.webank.wecross.account.uaproof;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class UAProofSign {
    private byte[] signBytes;
    private String signer;
    private String signee;
    private String timestamp;

    @JsonIgnore
    public byte[] getMessage() {
        String fullSignString = signee + "," + timestamp;
        return fullSignString.getBytes(StandardCharsets.UTF_8);
    }

    public byte[] getSignBytes() {
        return signBytes;
    }

    public void setSignBytes(byte[] signBytes) {
        this.signBytes = signBytes;
    }

    public String getSigner() {
        return signer;
    }

    public void setSigner(String signer) {
        this.signer = signer;
    }

    public String getSignee() {
        return signee;
    }

    public void setSignee(String signee) {
        this.signee = signee;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "UAProofSign{"
                + "signBytes="
                + Arrays.toString(signBytes)
                + ", signer='"
                + signer
                + '\''
                + ", signee='"
                + signee
                + '\''
                + ", timestamp='"
                + timestamp
                + '\''
                + '}';
    }
}
