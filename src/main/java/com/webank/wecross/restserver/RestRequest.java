package com.webank.wecross.restserver;

import com.webank.wecross.exception.WeCrossException;

public class RestRequest<T> {

    private String version;
    private T data;

    public void checkRestRequest() throws WeCrossException {
        String errorMessage;
        if (this.version == null) {
            errorMessage = "\"version\" not found in request package";
            throw new WeCrossException(WeCrossException.ErrorCode.FIELD_MISSING, errorMessage);
        }

        if (!Versions.checkVersion(version)) {
            errorMessage = "Unsupported version :" + version;
            throw new WeCrossException(WeCrossException.ErrorCode.VERSION_ERROR, errorMessage);
        }
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "RestRequest{" + "version='" + version + '\'' + ", data=" + data + '}';
    }
}
