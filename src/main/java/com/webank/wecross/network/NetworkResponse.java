package com.webank.wecross.network;

public class NetworkResponse<T> {
    private String version;
    private int seq;
    private int errorCode = 0;
    private String message;
    private T data;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "NetworkResponse{"
                + "version='"
                + version
                + '\''
                + ", seq="
                + seq
                + ", errorCode="
                + errorCode
                + ", message='"
                + message
                + '\''
                + ", data="
                + data
                + '}';
    }
}
