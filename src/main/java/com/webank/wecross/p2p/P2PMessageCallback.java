package com.webank.wecross.p2p;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.wecross.p2p.engine.P2PResponse;
import com.webank.wecross.p2p.netty.common.Peer;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;

public class P2PMessageCallback<T> {
    private TypeReference typeReference;
    protected int status;
    protected String message;
    protected P2PMessage<T> data;

    private Peer peer;

    public void execute() {
        this.onResponse(status, message, data);
    }

    public void onResponse(int status, String message, P2PMessage msg) {}

    public void setStatus(int status) {
        this.status = status;
    }

    public P2PResponse<Object> parseContent(String content) throws Exception {
        if (typeReference == null) {
            throw new Exception("Callback message type has not been set");
        }

        P2PResponse<Object> p2PResponse =
                (P2PResponse<Object>)
                        ObjectMapperFactory.getObjectMapper().readValue(content, typeReference);
        return p2PResponse;
    }

    @JsonIgnore
    public int getStatus() {
        return this.status;
    }

    public void setData(P2PMessage msg) {
        this.data = msg;
    }

    public void setTypeReference(TypeReference typeReference) {
        this.typeReference = typeReference;
    }

    public TypeReference getTypeReference() {
        return typeReference;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @JsonIgnore
    public String getMessage() {
        return this.message;
    }

    @JsonIgnore
    public Peer getPeer() {
        return peer;
    }

    @JsonIgnore
    public void setPeer(Peer peer) {
        this.peer = peer;
    }
}
