package com.webank.wecross.config;

import com.webank.wecross.network.rpc.RPCService;
import com.webank.wecross.network.rpc.URIHandlerDispatcher;
import com.webank.wecross.network.rpc.netty.RPCBootstrap;
import com.webank.wecross.network.rpc.netty.RPCConfig;
import com.webank.wecross.restserver.RPCContext;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RPCServiceConfig {
    @Resource RPCConfig rpcConfig;
    @Resource RPCContext rpcContext;

    @Bean
    public RPCService newRPCService() {
        RPCBootstrap rpcBootstrap = new RPCBootstrap();
        rpcBootstrap.setConfig(rpcConfig);
        rpcBootstrap.setRPCContext(rpcContext);

        URIHandlerDispatcher uriHandlerDispatcher = new URIHandlerDispatcher();
        rpcBootstrap.setUriHandlerDispatcher(uriHandlerDispatcher);

        RPCService rpcService = new RPCService();
        rpcService.setRpcBootstrap(rpcBootstrap);
        return rpcService;
    }
}
