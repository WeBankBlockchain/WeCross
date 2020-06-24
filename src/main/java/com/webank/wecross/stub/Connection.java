package com.webank.wecross.stub;

import java.util.List;
import java.util.Map;

public interface Connection {
    public interface ConnectionEventHandler {
        void onResourcesChange(List<ResourceInfo> resourceInfos);
    }

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
    @Deprecated
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
     * @return Map<String , String>
     */
    Map<String, String> getProperties();

    /**
     * set the callback of connection events
     *
     * @param eventHandler
     * @return
     */
    void setConnectionEventHandler(ConnectionEventHandler eventHandler);
}
