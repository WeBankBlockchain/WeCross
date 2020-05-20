package com.webank.wecross.network.rpc.handler;

import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.restserver.RestResponse;
import com.webank.wecross.restserver.request.StateRequest;
import com.webank.wecross.restserver.response.StateResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

/** GET/POST /state */
public class StateURIHandler implements URIHandler {

    private WeCrossHost host;

    public StateURIHandler() {}

    public StateURIHandler(WeCrossHost host) {
        this.host = host;
    }

    public WeCrossHost getHost() {
        return host;
    }

    public void setHost(WeCrossHost host) {
        this.host = host;
    }

    @Override
    public RestResponse<StateResponse> handle(
            ChannelHandlerContext ctx, FullHttpRequest httpRequest) {

        StateResponse stateResponse = host.getState(new StateRequest());
        RestResponse<StateResponse> restResponse = new RestResponse<>();
        restResponse.setData(stateResponse);

        return restResponse;
    }
}
