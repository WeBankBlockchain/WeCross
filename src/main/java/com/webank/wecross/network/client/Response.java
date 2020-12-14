package com.webank.wecross.network.client;

import java.util.Objects;

public class Response<T> {
    private String version;
    private int errorCode = -1;
    private String message;
    private T data;

    public Response() {}

    public Response(Integer errorCode, String message, T data) {
        this.errorCode = errorCode;
        this.message = message;
        this.data = data;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Response)) {
            return false;
        }
        Response<?> response = (Response<?>) o;
        return Objects.equals(getVersion(), response.getVersion())
                && Objects.equals(getErrorCode(), response.getErrorCode())
                && Objects.equals(getMessage(), response.getMessage())
                && Objects.equals(getData(), response.getData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVersion(), getErrorCode(), getMessage(), getData());
    }

    @Override
    public String toString() {
        return "Response{"
                + "version='"
                + version
                + '\''
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
