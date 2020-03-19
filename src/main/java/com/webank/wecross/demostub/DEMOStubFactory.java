package com.webank.wecross.demostub;

import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Stub;
import com.webank.wecross.stub.StubFactory;

@Stub("demo")
public class DEMOStubFactory implements StubFactory {

    @Override
    public Driver newDriver() {
        return new DEMODriver();
    }

    @Override
    public Connection newConnection(String path) {
        return new DEMOConnection();
    }

    @Override
    public Account newAccount(String name, String path) {
        return new DEMOAccount();
    }
}
