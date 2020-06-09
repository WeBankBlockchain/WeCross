package com.webank.wecross.stub;

import java.util.List;
import java.util.Map;

public interface Connection {

    // Callback for asyncSend
    interface Callback {
        void onResponse(Response response);
    }

    /**
     * send request to blockchain
     *
     * @param request
     * @return
     */
    Response send(Request request);

    /**
     * asyncSend request to blockchain
     *
     * @param request
     * @param callback
     * @return
     */
    default void asyncSend(Request request, Connection.Callback callback) {
        callback.onResponse(send(request));
    }

    /**
     * get resources name
     *
     * @return resources
     */
    List<ResourceInfo> getResources();

    /**
     * get properties
     *
     * @return Map<String, String>
     */
    Map<String, String> getProperties();
}
