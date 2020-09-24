package com.webank.wecross.network.rpc.handler;

import com.webank.wecross.restserver.RestResponse;
import com.webank.wecross.stub.UniversalAccount;

/** */
public interface URIHandler {
    interface Callback {
        void onResponse(RestResponse restResponse);
    }

    void handle(UniversalAccount ua, String uri, String method, String content, Callback callback);
}
