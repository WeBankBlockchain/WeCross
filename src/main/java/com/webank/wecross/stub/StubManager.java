package com.webank.wecross.stub;

import com.webank.wecross.exception.WeCrossException;
import java.util.HashMap;
import java.util.Map;

public class StubManager {
    private Map<String, StubFactory> drivers = new HashMap<String, StubFactory>();

    public void addStubFactory(String type, StubFactory stubFactory) {
        drivers.put(type, stubFactory);
    }

    public StubFactory getStubFactory(String type) throws WeCrossException {
        if (!drivers.containsKey(type)) {
            throw new WeCrossException(-1, "StubFactory[" + type + "] not found!");
        }

        return drivers.get(type);
    }

    public Map<String, StubFactory> getDrivers() {
        return drivers;
    }

    public void setDrivers(Map<String, StubFactory> drivers) {
        this.drivers = drivers;
    }
}
