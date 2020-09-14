package com.webank.wecross.network.client;

import java.util.HashMap;
import java.util.Map;

public class Request<T> {
    private String version = "1.0";
    private String method;
    private Map<String, String> properties = new HashMap<>();
    private T data;

    public Request() {}

    public Request(String method, T data) {
        this.method = method;
        this.data = data;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    public void setProperty(String key, String value) {
        properties.putIfAbsent(key, value);
    }
}
