package com.webank.wecross.network.rpc.handler;

import com.webank.wecross.restserver.RestResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

/** GET/POST /test */
public class TestURIHandler implements URIHandler {

    @Override
    public RestResponse handle(ChannelHandlerContext ctx, FullHttpRequest httpRequest) {
        RestResponse<String> restResponse = new RestResponse<>();
        restResponse.setData("OK!");

        return restResponse;
    }
}
