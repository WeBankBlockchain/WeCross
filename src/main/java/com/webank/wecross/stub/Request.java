package com.webank.wecross.stub;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Request {
    private int type;
    private String path;
    private byte[] data;
    private ResourceInfo resourceInfo;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public ResourceInfo getResourceInfo() {
        return resourceInfo;
    }

    public void setResourceInfo(ResourceInfo resourceInfo) {
        this.resourceInfo = resourceInfo;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "Request{"
                + "type="
                + type
                + ", path='"
                + path
                + '\''
                + ", data="
                + Arrays.toString(data)
                + ", resourceInfo="
                + resourceInfo
                + '}';
    }

    /**
     * construct Request object
     *
     * @param type
     * @param content
     * @return
     */
    public static Request newRequest(int type, String content) {
        return newRequest(type, content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * construct Request object
     *
     * @param type
     * @param content
     * @return
     */
    public static Request newRequest(int type, byte[] content) {
        Request request = new Request();
        request.setType(type);
        request.setData(content);
        return request;
    }
}
