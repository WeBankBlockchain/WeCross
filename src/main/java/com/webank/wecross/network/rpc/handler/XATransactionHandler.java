package com.webank.wecross.network.rpc.handler;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.account.AccountManager;
import com.webank.wecross.restserver.RestRequest;
import java.io.IOException;
import java.util.List;

public class XATransactionHandler implements URIHandler {
    public class XAPrepareRequest {
        private String transactionID;
        private List<String> resources;

        public String getTransactionID() {
            return transactionID;
        }

        public void setTransactionID(String transactionID) {
            this.transactionID = transactionID;
        }

        public List<String> getResources() {
            return resources;
        }

        public void setResources(List<String> resources) {
            this.resources = resources;
        }
    }

    private ObjectMapper objectMapper = new ObjectMapper();
    private AccountManager accountManager;

    @Override
    public void handle(String uri, String method, String content, Callback callback) {
        // callback.onResponse(restResponse);
        try {
            RestRequest<XAPrepareRequest> xaRequest =
                    objectMapper.readValue(
                            content, new TypeReference<RestRequest<XAPrepareRequest>>() {});

            // Account account = accountManager.getAccount(xaRequest.getAccountName());

        } catch (JsonParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
