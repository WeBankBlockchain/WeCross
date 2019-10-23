package com.webank.wecross.resource.response;

import com.webank.wecross.resource.Resource;
import java.util.List;

public class ResourceResponse {

    private Integer errorCode;
    private String errorMessage;

    private List<Resource> resources;

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

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }
}
