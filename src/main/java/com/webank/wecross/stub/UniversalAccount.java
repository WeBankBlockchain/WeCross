package com.webank.wecross.stub;

import com.fasterxml.jackson.annotation.JsonGetter;
import java.util.List;

public interface UniversalAccount {

    String getName();

    String getUAID();

    String getPub();

    byte[] sign(byte[] message);

    boolean verify(byte[] signData, byte[] originData);

    Account getAccount(String type);

    @JsonGetter("chainAccounts")
    List<Account> getAccounts();
}
