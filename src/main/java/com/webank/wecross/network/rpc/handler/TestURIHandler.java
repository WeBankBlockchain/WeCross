package com.webank.wecross.network.rpc.handler;

import com.webank.wecross.restserver.RestResponse;

/** GET/POST /test */
public class TestURIHandler implements URIHandler {

    @Override
    public void handle(String uri, String method, String content, Callback callback) {
        RestResponse<String> restResponse = new RestResponse<>();
        restResponse.setData("OK!");

        callback.onResponse(restResponse);
    }
}
