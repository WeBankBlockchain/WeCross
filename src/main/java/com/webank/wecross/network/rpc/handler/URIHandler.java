package com.webank.wecross.network.rpc.handler;

import com.webank.wecross.restserver.RestResponse;
import io.netty.handler.codec.http.FullHttpRequest;

/** */
public interface URIHandler {
    interface Callback {
        void onResponse(RestResponse restResponse);
    }

    void handle(FullHttpRequest httpRequest, Callback callback);
}
