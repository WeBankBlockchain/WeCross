package com.webank.wecross.resource;

import java.util.Map;

public class ResourceInfo {
    private String path;
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
    public boolean equals(Object obj) {
        try {
            // no need to check distance
            return ((ResourceInfo) obj).path.equals(this.path)
                    && ((ResourceInfo) obj).checksum.equals(this.checksum);

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
