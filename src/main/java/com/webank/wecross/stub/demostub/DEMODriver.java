package com.webank.wecross.stub.demostub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.Response;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import java.io.IOException;

public class DEMODriver implements Driver {
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public byte[] encodeTransactionRequest(TransactionContext<TransactionRequest> request) {
        try {
            return mapper.writeValueAsBytes(request);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @Override
    public TransactionContext<TransactionRequest> decodeTransactionRequest(byte[] data) {
        try {
            return mapper.readValue(
                    data, new TypeReference<TransactionContext<TransactionRequest>>() {});
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public byte[] encodeTransactionResponse(TransactionResponse response) {
        try {
            return mapper.writeValueAsBytes(response);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @Override
    public TransactionResponse decodeTransactionResponse(byte[] data) {
        try {
            return mapper.readValue(data, TransactionResponse.class);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public BlockHeader decodeBlockHeader(byte[] data) {
        try {
            return mapper.readValue(data, BlockHeader.class);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public TransactionResponse call(
            TransactionContext<TransactionRequest> request, Connection connection) {
        byte[] data = encodeTransactionRequest(request);

        Request connectionRequest = new Request();
        connectionRequest.setData(data);
        Response connectionResponse = connection.send(connectionRequest);

        TransactionResponse response = decodeTransactionResponse(connectionResponse.getData());

        return response;
    }

    @Override
    public TransactionResponse sendTransaction(
            TransactionContext<TransactionRequest> request, Connection connection) {
        byte[] data = encodeTransactionRequest(request);

        Request connectionRequest = new Request();
        connectionRequest.setData(data);
        Response connectionResponse = connection.send(connectionRequest);

        TransactionResponse response = decodeTransactionResponse(connectionResponse.getData());

        return response;
    }

    @Override
    public long getBlockNumber(Connection connection) {
        return 0;
    }

    @Override
    public byte[] getBlockHeader(long number, Connection connection) {
        return null;
    }

    @Override
    public boolean isTransaction(Request request) {
        return false;
    }
}
