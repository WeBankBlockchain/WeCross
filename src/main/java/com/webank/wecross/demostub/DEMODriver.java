package com.webank.wecross.demostub;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.BlockHeaderManager;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.Response;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import com.webank.wecross.stub.VerifiedTransaction;
import java.io.IOException;

public class DEMODriver implements Driver {
    private ObjectMapper mapper = new ObjectMapper();

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
        try {
            byte[] data = mapper.writeValueAsBytes(request);

            Request connectionRequest = new Request();
            connectionRequest.setData(data);
            Response connectionResponse = connection.send(connectionRequest);

            TransactionResponse response =
                    response =
                            mapper.readValue(
                                    connectionResponse.getData(),
                                    new TypeReference<TransactionResponse>() {});

            return response;

        } catch (IOException e) {
        }

        return null;
    }

    @Override
    public TransactionResponse sendTransaction(
            TransactionContext<TransactionRequest> request, Connection connection) {
        try {
            byte[] data = mapper.writeValueAsBytes(request);

            Request connectionRequest = new Request();
            connectionRequest.setData(data);
            Response connectionResponse = connection.send(connectionRequest);

            TransactionResponse response =
                    mapper.readValue(
                            connectionResponse.getData(),
                            new TypeReference<TransactionResponse>() {});
            return response;
        } catch (IOException e) {
        }

        return null;
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
    public VerifiedTransaction getVerifiedTransaction(
            String transactionHash, BlockHeaderManager blockHeaderManager, Connection connection) {
        return null;
    }

    @Override
    public boolean isTransaction(Request request) {
        return false;
    }
}
