package com.webank.wecross.interchain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.exception.WeCrossException.ErrorCode;
import java.util.Arrays;
import java.util.Objects;

public class InterchainRequest {
    private ObjectMapper objectMapper = new ObjectMapper();

    private String uid;
    private int callType;
    private String path;
    private String method;
    private String[] args;
    private String callbackPath;
    private String callbackMethod;
    private String identity;

    public void build(String jsonRequest) throws WeCrossException {
        try {
            String[] request = objectMapper.readValue(jsonRequest, String[].class);
            if (Objects.isNull(request) || request.length != 8) {
                throw new Exception(jsonRequest + " is a invalid interchain request");
            }

            uid = request[0];
            callType = Integer.parseInt(request[1]);
            path = request[2];
            method = request[3];
            args = objectMapper.readValue(request[4], String[].class);
            if (Objects.isNull(args)) {
                throw new Exception(request[4] + " is a invalid args of interchain request");
            }

            callbackPath = request[5];
            callbackMethod = request[6];
            identity = request[7];
        } catch (Exception e) {
            throw new WeCrossException(
                    ErrorCode.INTER_CHAIN_ERROR,
                    "DECODE_REQUEST_ERROR",
                    InterchainErrorCode.DECODE_REQUEST_ERROR,
                    e.getMessage());
        }
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getCallType() {
        return callType;
    }

    public void setCallType(int callType) {
        this.callType = callType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public String getCallbackPath() {
        return callbackPath;
    }

    public void setCallbackPath(String callbackPath) {
        this.callbackPath = callbackPath;
    }

    public String getCallbackMethod() {
        return callbackMethod;
    }

    public void setCallbackMethod(String callbackMethod) {
        this.callbackMethod = callbackMethod;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    @Override
    public String toString() {
        return "InterchainRequest{"
                + "uid='"
                + uid
                + '\''
                + ", callType="
                + callType
                + ", path='"
                + path
                + '\''
                + ", method='"
                + method
                + '\''
                + ", args="
                + Arrays.toString(args)
                + ", callbackPath='"
                + callbackPath
                + '\''
                + ", callbackMethod='"
                + callbackMethod
                + '\''
                + ", identity='"
                + identity
                + '\''
                + '}';
    }
}
