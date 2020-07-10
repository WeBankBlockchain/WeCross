package com.webank.wecross.network;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.utils.ObjectMapperFactory;

public abstract class NetworkCallback<T> {
    private TypeReference<?> typeReference;
    protected int status;
    protected String message;
    protected NetworkResponse<T> data;
    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    public void execute() {
        this.onResponse(status, message, data);
    }

    public abstract void onResponse(int status, String message, NetworkResponse<T> msg);

    public void setStatus(int status) {
        this.status = status;
    }

    public NetworkResponse<Object> parseContent(String content) throws Exception {
        if (typeReference == null) {
            throw new Exception("Callback message type has not been set");
        }

        NetworkResponse<Object> networkResponse =
                (NetworkResponse<Object>) objectMapper.readValue(content, typeReference);
        return networkResponse;
    }

    @JsonIgnore
    public int getStatus() {
        return this.status;
    }

    public void setData(NetworkResponse<T> msg) {
        this.data = msg;
    }

    public void setTypeReference(TypeReference<?> typeReference) {
        this.typeReference = typeReference;
    }

    public TypeReference<?> getTypeReference() {
        return typeReference;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @JsonIgnore
    public String getMessage() {
        return this.message;
    }
}
