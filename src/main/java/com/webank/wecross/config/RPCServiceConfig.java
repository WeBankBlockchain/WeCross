package com.webank.wecross.config;

import com.webank.wecross.account.AccountManager;
import com.webank.wecross.network.rpc.RPCService;
import com.webank.wecross.network.rpc.URIHandlerDispatcher;
import com.webank.wecross.network.rpc.authentication.AuthFilter;
import com.webank.wecross.network.rpc.netty.RPCBootstrap;
import com.webank.wecross.network.rpc.netty.RPCConfig;
import com.webank.wecross.network.rpc.web.WebService;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RPCServiceConfig {
    @Resource RPCConfig rpcConfig;

    @Resource AuthFilter authFilter;

    @Resource AccountManager accountManager;

    @Resource WebService webService;

    @Bean
    public RPCService newRPCService() {
        RPCBootstrap rpcBootstrap = new RPCBootstrap();
        rpcBootstrap.setConfig(rpcConfig);
        rpcBootstrap.setAccountManager(accountManager);
        rpcBootstrap.setAuthFilter(authFilter);

        URIHandlerDispatcher uriHandlerDispatcher = new URIHandlerDispatcher();
        uriHandlerDispatcher.setWebService(webService);

        rpcBootstrap.setUriHandlerDispatcher(uriHandlerDispatcher);

        RPCService rpcService = new RPCService();
        rpcService.setRpcBootstrap(rpcBootstrap);
        return rpcService;
    }
}
