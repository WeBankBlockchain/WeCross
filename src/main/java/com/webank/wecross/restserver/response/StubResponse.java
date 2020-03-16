package com.webank.wecross.restserver.response;

import com.webank.wecross.stub.StubFactory;
import com.webank.wecross.stub.StubManager;
import java.util.Map;

public class StubResponse {
    String[] stubs;

    public String[] getStubs() {
        return stubs;
    }

    public void setStubs(String[] stubs) {
        this.stubs = stubs;
    }

    public void setStubs(StubManager stubManager) {
        Map<String, StubFactory> stubMap = stubManager.getDrivers();
        this.stubs = stubMap.keySet().toArray(new String[stubMap.size()]);
    }
}
