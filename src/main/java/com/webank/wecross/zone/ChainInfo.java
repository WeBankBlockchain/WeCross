package com.webank.wecross.zone;

import com.webank.wecross.stub.ResourceInfo;
import java.util.List;
import java.util.Map;

public class ChainInfo {
    private String name;
    private String stubType;
    private List<ResourceInfo> resources;
    private Map<String, String> properties;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStubType() {
        return stubType;
    }

    public void setStubType(String stubType) {
        this.stubType = stubType;
    }

    public List<ResourceInfo> getResources() {
        return resources;
    }

    public void setResources(List<ResourceInfo> resources) {
        this.resources = resources;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
