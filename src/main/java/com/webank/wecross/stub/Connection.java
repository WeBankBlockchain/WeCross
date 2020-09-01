package com.webank.wecross.stub;

import java.util.List;

public interface Connection {
    public interface ConnectionEventHandler {
        void onResourcesChange(List<ResourceInfo> resourceInfos);
    }

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

    /**
     * set the callback of connection events
     *
     * @param eventHandler
     * @return
     */
    void setConnectionEventHandler(ConnectionEventHandler eventHandler);
}
