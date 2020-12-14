package com.webank.wecross.account.uaproof;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UAProof {
    @JsonIgnore private static ObjectMapper objectMapper = new ObjectMapper();

    private String type;
    private UAProofSign ua2ca; // universal account to chain account
    private UAProofSign ca2ua; // chain account to universal account

    @JsonIgnore
    public String getUaID() {
        return ca2ua.getSignee();
    }

    @JsonIgnore
    public String getCaID() {
        return ua2ca.getSignee();
    }

    public static UAProof perseFrom(String jsonString)
            throws JsonProcessingException, JsonMappingException {
        return objectMapper.readValue(jsonString, new TypeReference<UAProof>() {});
    }

    public String toJsonString() throws JsonProcessingException {
        return objectMapper.writeValueAsString(this);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UAProofSign getUa2ca() {
        return ua2ca;
    }

    public void setUa2ca(UAProofSign ua2ca) {
        this.ua2ca = ua2ca;
    }

    public UAProofSign getCa2ua() {
        return ca2ua;
    }

    public void setCa2ua(UAProofSign ca2ua) {
        this.ca2ua = ca2ua;
    }

    @Override
    public String toString() {
        return "UAProof{" + "type='" + type + '\'' + ", ua2ca=" + ua2ca + ", ca2ua=" + ca2ua + '}';
    }
}
