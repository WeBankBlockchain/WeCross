package com.webank.wecross.stub;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ResourceInfo {
    private String name;
    private String stubType;
    private Map<Object, Object> properties = new HashMap<Object, Object>();
    private String checksum;

    public static boolean isEqualInfos(Map<String, ResourceInfo> a, Map<String, ResourceInfo> b) {
        if (a.size() != b.size()) {
            return false;
        }

        for (Map.Entry<String, ResourceInfo> info : a.entrySet()) {
            ResourceInfo bInfo = b.get(info.getKey());
            if (bInfo == null) {
                return false;
            }

            if (!info.getValue().equals(bInfo)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = result * 31 + (this.name == null ? 0 : this.name.hashCode());
        result = result * 31 + (this.checksum == null ? 0 : this.checksum.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        try {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof ResourceInfo)) {
                return false;
            }

            ResourceInfo info = (ResourceInfo) obj;
            // no need to check distance
            return info.name.equals(this.name) && Objects.equals(info.checksum, this.checksum);

        } catch (Exception e) {
            return false;
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getStubType() {
        return stubType;
    }

    public void setStubType(String driverType) {
        this.stubType = driverType;
    }

    @JsonIgnore
    public Map<Object, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<Object, Object> properties) {
        this.properties = properties;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getChecksum() {
        return checksum;
    }

    @Override
    public String toString() {
        return "ResourceInfo{"
                + "name='"
                + name
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
