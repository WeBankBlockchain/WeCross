package com.webank.wecross.network.rpc.factory;

import com.webank.wecross.network.rpc.URIHandlerDispatcher;

public class URIDispatcherFactory {
    public static URIHandlerDispatcher build() {
        return new URIHandlerDispatcher();
    }
}
