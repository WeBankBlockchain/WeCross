package com.webank.wecross.network.rpc.handler;

import com.webank.wecross.account.UserContext;
import com.webank.wecross.restserver.RestResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import java.io.File;

/** */
public interface URIHandler {
    interface Callback {
        void onResponse(File restResponse);

        void onResponse(RestResponse restResponse);

        void onResponse(String restResponse);

        void onResponse(FullHttpResponse fullHttpResponse);
    }

    void handle(
            UserContext userContext, String uri, String method, String content, Callback callback);
}
