package com.webank.wecross.network.rpc.handler;

import com.webank.wecross.restserver.RestResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

/** */
public interface URIHandler {
    RestResponse handle(ChannelHandlerContext ctx, FullHttpRequest httpRequest);
}
