package com.webank.wecross.stub;

import java.util.List;
import java.util.Map;

public interface Connection {

    // Callback for asyncSend
    interface Callback {
        void onResponse(Response response);
    }

    /**
     * asyncSend request to blockchain
     *
     * @param request
     * @param callback
     * @return
     */
    void asyncSend(Request request, Connection.Callback callback);

    // Callback for setConnectionEventHandler
    interface ConnectionEventHandler {
        void onResourcesChange(List<ResourceInfo> resourceInfos);
    }
    /**
     * set the callback of connection events
     *
     * @param eventHandler
     * @return
     */
    void setConnectionEventHandler(ConnectionEventHandler eventHandler);

    /**
     * get properties
     *
     * @return Map<String , String>
     */
    Map<String, String> getProperties();
}
