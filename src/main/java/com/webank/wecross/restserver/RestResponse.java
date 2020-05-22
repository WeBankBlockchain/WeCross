package com.webank.wecross.restserver;

import com.webank.wecross.common.NetworkQueryStatus;

public class RestResponse<T> {
    private String version = Versions.currentVersion;
    private Integer errorCode = NetworkQueryStatus.SUCCESS;
    private String message = NetworkQueryStatus.getStatusMessage(NetworkQueryStatus.SUCCESS);
    private T data;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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
}
