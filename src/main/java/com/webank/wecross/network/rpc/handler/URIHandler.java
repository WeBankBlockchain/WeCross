package com.webank.wecross.network.rpc.handler;

import com.webank.wecross.restserver.RestResponse;

/** */
public interface URIHandler {
    interface Callback {
        void onResponse(RestResponse restResponse);
    }

    void handle(String uri, String method, String content, Callback callback);
}
