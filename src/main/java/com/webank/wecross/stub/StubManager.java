package com.webank.wecross.stub;

import com.webank.wecross.exception.WeCrossException;
import java.util.HashMap;
import java.util.Map;

public class StubManager {
    private Map<String, com.webank.wecross.stub.StubFactory> drivers = new HashMap<>();

    public void addStubFactory(String type, com.webank.wecross.stub.StubFactory stubFactory) {
        drivers.put(type, stubFactory);
    }

    public com.webank.wecross.stub.StubFactory getStubFactory(String type) throws WeCrossException {
        if (!drivers.containsKey(type)) {
            throw new WeCrossException(-1, "StubFactory[" + type + "] not found!");
        }

        return drivers.get(type);
    }

    public Map<String, com.webank.wecross.stub.StubFactory> getDrivers() {
        return drivers;
    }

    public void setDrivers(Map<String, com.webank.wecross.stub.StubFactory> drivers) {
        this.drivers = drivers;
    }
}
