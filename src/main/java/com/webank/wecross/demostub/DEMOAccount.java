package com.webank.wecross.demostub;

import com.webank.wecross.stub.Account;

public class DEMOAccount implements Account {

    @Override
    public String getName() {
        return "DEMOAccount";
    }

    @Override
    public String getType() {
        return "DEMOAccount";
    }

    @Override
    public String getIdentity() {
        return "0x0";
    }
}
