package com.webank.wecross.network.client;

import com.webank.wecross.exception.WeCrossException;

public interface ClientMessageEngine {
    void init() throws WeCrossException;

    <T extends Response> T send(Request request) throws Exception;

    <T extends Response> void asyncSend(Request<?> request, Callback<T> callback);
}
