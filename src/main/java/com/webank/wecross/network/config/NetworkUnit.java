package com.webank.wecross.network.config;

import java.util.Map;

public class NetworkUnit {

    private Boolean visible = false;
    private Map<String, Object> stubs;

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Map<String, Object> getStubs() {
        return stubs;
    }

    public void setStubs(Map<String, Object> stubs) {
        this.stubs = stubs;
    }
}
