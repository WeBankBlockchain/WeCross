package com.webank.wecross.network.client;

public class Request<T> {
    private String version = "1.0";
    private String method;
    private String auth = "";
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

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }
}
