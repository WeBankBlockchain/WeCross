package com.webank.wecross.stub.demostub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.Response;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DEMOConnection implements Connection {
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public Response send(Request request) {
        try {
            TransactionRequest transactionRequest =
                    mapper.readValue(request.getData(), TransactionRequest.class);

            TransactionResponse transactionResponse = new TransactionResponse();
            transactionResponse.setErrorCode(0);
            transactionResponse.setResult(transactionRequest.getArgs());

            Response response = new Response();
            response.setData(mapper.writeValueAsBytes(transactionResponse));

            return response;
        } catch (IOException e) {
        }

        return null;
    }

    @Override
    public List<ResourceInfo> getResources() {
        ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.setName("demo");
        List<ResourceInfo> resources = new ArrayList<ResourceInfo>();
        resources.add(resourceInfo);

        return resources;
    }
}
