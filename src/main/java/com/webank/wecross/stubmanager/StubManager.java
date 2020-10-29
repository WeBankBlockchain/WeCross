package com.webank.wecross.stubmanager;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.StubFactory;
import java.util.HashMap;
import java.util.Map;

public class StubManager {
    private Map<String, StubFactory> stubFactories = new HashMap<>();
    private Map<String, Driver> drivers = new HashMap<>();

    public void addStubFactory(String type, StubFactory stubFactory) {
        stubFactories.put(type, stubFactory);
    }

    public StubFactory getStubFactory(String type) throws WeCrossException {
        if (!stubFactories.containsKey(type)) {
            throw new WeCrossException(-1, "Stub plugin[" + type + "] not found!");
        }

        return stubFactories.get(type);
    }

    public Driver getStubDriver(String type) throws WeCrossException {
        if (!drivers.containsKey(type)) {
            drivers.put(type, getStubFactory(type).newDriver());
        }
        return drivers.get(type);
    }

    public Connection newStubConnection(String type, String stubPath) throws WeCrossException {
        return getStubFactory(type).newConnection(stubPath);
    }

    public Account newStubAccount(String type, Map<String, Object> properties)
            throws WeCrossException {
        return getStubFactory(type).newAccount(properties);
    }

    public Map<String, StubFactory> getStubFactories() {
        return stubFactories;
    }

    public void setStubFactories(Map<String, StubFactory> stubFactories) {
        this.stubFactories = stubFactories;
    }

    public boolean hasFactory(String type) {
        return stubFactories.containsKey(type);
    }
}
