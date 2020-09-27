package com.webank.wecross.account.uaproof;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

@Data
public class UAProof {
    @JsonIgnore private static ObjectMapper objectMapper = new ObjectMapper();

    private String type;
    private UAProofSign ua2ca; // universal account to chain account
    private UAProofSign ca2ua; // chain account to universal account

    public String getUAID() {
        return ca2ua.getSignee();
    }

    public String getCAID() {
        return ua2ca.getSignee();
    }

    public static UAProof perseFrom(String jsonString)
            throws JsonProcessingException, JsonMappingException {
        return objectMapper.readValue(jsonString, new TypeReference<UAProof>() {});
    }

    public String toJsonString() throws JsonProcessingException {
        return objectMapper.writeValueAsString(this);
    }
}
