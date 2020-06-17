package com.webank.wecross.restserver.response;

import com.webank.wecross.stub.StubFactory;
import com.webank.wecross.stubmanager.StubManager;
import java.util.Map;

public class StubResponse {
    String[] stubTypes;

    public String[] getStubTypes() {
        return stubTypes;
    }

    public void setStubTypes(String[] stubTypes) {
        this.stubTypes = stubTypes;
    }

    public void setStubTypes(StubManager stubManager) {
        Map<String, StubFactory> stubMap = stubManager.getDrivers();
        this.stubTypes = stubMap.keySet().toArray(new String[stubMap.size()]);
    }
}
