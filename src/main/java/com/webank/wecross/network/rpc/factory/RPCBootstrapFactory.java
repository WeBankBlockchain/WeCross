package com.webank.wecross.network.rpc.factory;

import com.webank.wecross.network.rpc.netty.RPCBootstrap;

public class RPCBootstrapFactory {
    public static RPCBootstrap build() {
        return new RPCBootstrap();
    }
}
