package com.webank.wecross.stub.demostub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.Response;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;

import java.io.IOException;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;

public class DEMODriver implements Driver {
    private ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();

    public byte[] encodeTransactionRequest(TransactionRequest request) {
        try {
            return mapper.writeValueAsBytes(request);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @Override
    public TransactionRequest decodeTransactionRequest(byte[] data) {
        try {
            return mapper.readValue(data, TransactionRequest.class);
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
    public byte[] encodeBlockHeader(BlockHeader block) {
        try {
            return mapper.writeValueAsBytes(block);
        } catch (JsonProcessingException e) {
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
    public TransactionResponse call(TransactionRequest request, Connection connection) {
        byte[] data = encodeTransactionRequest(request);

        Request connectionRequest = new Request();
        connectionRequest.setData(data);
        Response connectionResponse = connection.send(connectionRequest);

        TransactionResponse response = decodeTransactionResponse(connectionResponse.getData());

        return response;
    }

    @Override
    public TransactionResponse sendTransaction(TransactionRequest request, Connection connection) {
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
    public BlockHeader getBlockHeader(long number, Connection connection) {
        return null;
    }
}
