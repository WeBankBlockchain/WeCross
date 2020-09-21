package com.webank.wecross.stub;

public interface Account {
    String getName();

    String getType();
    /**
     * get the account's identity ( public key )
     *
     * @return
     */
    String getIdentity();

    int getKeyID();

    boolean isDefault();
}
