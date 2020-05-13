package com.webank.wecross.network;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.restserver.Versions;
import com.webank.wecross.utils.core.SeqUtils;

public class NetworkMessage<T> {

    private String version;
    private Integer seq;
    private String method;
    private T data;

    public NetworkMessage() {
        this.seq = 0;
    }

    public void newSeq() {

        this.seq = SeqUtils.newSeq();
    }

    public void checkP2PMessage(String method) throws WeCrossException {
        String errorMessage;
        if (this.version == null) {
            errorMessage = "\"version\" not found in request package";
            throw new WeCrossException(WeCrossException.ErrorCode.FIELD_MISSING, errorMessage);
        }

        if (this.seq == null) {
            errorMessage = "\"seq\" not found in request package";
            throw new WeCrossException(WeCrossException.ErrorCode.FIELD_MISSING, errorMessage);
        }

        if (this.method == null) {
            errorMessage = "\"method\" not found in request package";
            throw new WeCrossException(WeCrossException.ErrorCode.FIELD_MISSING, errorMessage);
        }

        if (!Versions.checkVersion(version)) {
            errorMessage = "Unsupported version :" + version;
            throw new WeCrossException(WeCrossException.ErrorCode.VERSION_ERROR, errorMessage);
        }

        String methods[] = this.method.split("/");

        if (!methods[methods.length - 1].equals(method)) {
            errorMessage = "Expect method: " + method;
            throw new WeCrossException(WeCrossException.ErrorCode.METHOD_ERROR, errorMessage);
        }
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
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
}
