package com.webank.wecross.stub.remote;

import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.wecross.network.config.ConfigType;
import com.webank.wecross.p2p.engine.P2PResponse;
import com.webank.wecross.restserver.response.TransactionResponse;
import com.webank.wecross.stub.bcos.BCOSResponse;
import com.webank.wecross.stub.jdchain.JDChainResponse;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;

public class RemoteTransactionResponseCallback extends RemoteSemaphoreCallback {

    public RemoteTransactionResponseCallback() {
        super(new TypeReference<P2PResponse<TransactionResponse>>() {});
    }

    @Override
    public P2PResponse<Object> parseContent(String content) throws Exception {
        P2PResponse<TransactionResponse> normalResponse =
                ObjectMapperFactory.getObjectMapper()
                        .readValue(
                                content, new TypeReference<P2PResponse<TransactionResponse>>() {});

        switch (normalResponse.getData().getType()) {
            case ConfigType.TRANSACTION_RSP_TYPE_BCOS:
                {
                    return ObjectMapperFactory.getObjectMapper()
                            .readValue(content, new TypeReference<P2PResponse<BCOSResponse>>() {});
                }
            case ConfigType.TRANSACTION_RSP_TYPE_JDCHAIN:
                {
                    return ObjectMapperFactory.getObjectMapper()
                            .readValue(
                                    content, new TypeReference<P2PResponse<JDChainResponse>>() {});
                }
            default:
                break;
        }

        return ObjectMapperFactory.getObjectMapper()
                .readValue(content, new TypeReference<P2PResponse<TransactionResponse>>() {});
    }
}
