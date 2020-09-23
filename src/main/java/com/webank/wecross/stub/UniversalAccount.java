package com.webank.wecross.stub;

public interface UniversalAccount {
    String getUAID();

    String getPub();

    byte[] sign(byte[] message);

    boolean verify(byte[] signData);

    Account getAccount(String type);
}
