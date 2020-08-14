package com.webank.wecross.resource;

import com.webank.wecross.stub.ResourceInfo;
import java.util.HashMap;
import java.util.Map;

public class ResourceDetail {
    private String path;
    private int distance;
    private String stubType;
    private Map<Object, Object> properties = new HashMap<Object, Object>();
    private String checksum;

    public ResourceDetail initResourceDetail(Resource resource, String path) {
        ResourceDetail detail = new ResourceDetail();
        int distance = resource.hasLocalConnection() ? 0 : 1;
        ResourceInfo resourceInfo = resource.getResourceInfo();
        detail.setPath(path);
        detail.setDistance(distance);
        detail.setStubType(resourceInfo.getStubType());
        detail.setProperties(resourceInfo.getProperties());
        detail.setChecksum(resourceInfo.getChecksum());
        return detail;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public String getStubType() {
        return stubType;
    }

    public void setStubType(String stubType) {
        this.stubType = stubType;
    }

    public Map<Object, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<Object, Object> properties) {
        this.properties = properties;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    @Override
    public String toString() {
        return "ResourceDetail{"
                + "path='"
                + path
                + '\''
                + ", distance="
                + distance
                + '\''
                + ", stubType='"
                + stubType
                + '\''
                + ", properties="
                + properties
                + ", checksum='"
                + checksum
                + '\''
                + '}';
    }
}
