package com.webank.wecross.account;

public interface Account {
    String getName();

    String getType();
    /**
     * get the account's identity ( public key )
     *
     * @return
     */
    String getIdentity();
}
