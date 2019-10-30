package com.webank.wecross.restserver.response;

import java.util.Arrays;

public class TransactionResponse {

    @Override
    public String toString() {
        return "TransactionResponse [errorCode="
                + errorCode
                + ", errorMessage="
                + errorMessage
                + ", hash="
                + hash
                + ", result="
                + Arrays.toString(result)
                + "]";
    }

    private Integer errorCode;
    private String errorMessage;
    private String hash;
    private Object result[];

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Object[] getResult() {
        return result;
    }

    public void setResult(Object result[]) {
        this.result = result;
    }
}
