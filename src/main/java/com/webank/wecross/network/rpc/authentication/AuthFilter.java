package com.webank.wecross.network.rpc.authentication;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;

public interface AuthFilter {
    interface Handler {
        void onAuth(ChannelHandlerContext ctx, HttpRequest httpRequest) throws Exception;
    }

    void doAuth(ChannelHandlerContext ctx, HttpRequest httpRequest, Handler handler)
            throws Exception;

    void registerAuthUri(String uri);
}
