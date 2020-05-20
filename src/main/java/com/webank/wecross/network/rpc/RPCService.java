package com.webank.wecross.network.rpc;

import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.network.rpc.netty.RPCBootstrap;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RPCService {

    private static final Logger logger = LoggerFactory.getLogger(RPCService.class);

    private RPCBootstrap rpcBootstrap;
    private WeCrossHost weCrossHost;

    public RPCBootstrap getRpcBootstrap() {
        return rpcBootstrap;
    }

    public void setRpcBootstrap(RPCBootstrap rpcBootstrap) {
        this.rpcBootstrap = rpcBootstrap;
    }

    public WeCrossHost getWeCrossHost() {
        return weCrossHost;
    }

    public void setWeCrossHost(WeCrossHost weCrossHost) {
        this.weCrossHost = weCrossHost;
    }

    public void start() throws InterruptedException, ExecutionException, IOException {
        rpcBootstrap.start();
        logger.info(" RPCService start. ");
    }
}
