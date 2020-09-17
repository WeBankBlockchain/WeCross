package com.webank.wecross.network.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.wecross.exception.WeCrossException;

public interface ClientMessageEngine extends BaseClientEngine {
    void init() throws WeCrossException;

    <T extends Response> T send(Request request, TypeReference typeReference) throws Exception;

    <T extends Response> void asyncSend(
            Request<?> request, TypeReference typeReference, Callback<T> callback);
}
