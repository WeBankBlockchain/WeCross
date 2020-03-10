package com.webank.wecross.resource;

import java.util.Map;

public class ResourceInfo {
    private String path;
    private String stubType;
    private int distance;
    private String checksum;

    public ResourceInfo() {}

    public ResourceInfo(String path) {
        this.path = path;
    }

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
        result = result * 31 + (this.path == null ? 0 : this.path.hashCode());
        result = result * 31 + (this.checksum == null ? 0 : this.checksum.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        try {
            if (obj == this) return true;
            if (!(obj instanceof ResourceInfo)) {
                return false;
            }

            ResourceInfo info = (ResourceInfo) obj;
            // no need to check distance
            return info.path.equals(this.path) && info.checksum.equals(this.checksum);

        } catch (Exception e) {
            return false;
        }
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
    
    public String getStubType() {
		return stubType;
	}

	public void setStubType(String driverType) {
		this.stubType = driverType;
	}

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getDistance() {
        return distance;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getChecksum() {
        return checksum;
    }
}
