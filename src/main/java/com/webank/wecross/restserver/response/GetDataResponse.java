package com.webank.wecross.restserver.response;

public class GetDataResponse {
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

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Object[] getResult() {
        return result;
    }

    public void setResult(Object[] result) {
        this.result = result;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
