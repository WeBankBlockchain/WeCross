package com.webank.wecross.config;

import com.webank.wecross.account.AccountManager;
import com.webank.wecross.account.UserContext;
import com.webank.wecross.network.rpc.RPCService;
import com.webank.wecross.network.rpc.URIHandlerDispatcher;
import com.webank.wecross.network.rpc.authentication.AuthFilter;
import com.webank.wecross.network.rpc.netty.RPCBootstrap;
import com.webank.wecross.network.rpc.netty.RPCConfig;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RPCServiceConfig {
    @Resource RPCConfig rpcConfig;

    @Resource(name = "newUserContext")
    UserContext userContext;

    @Resource AuthFilter authFilter;

    @Resource AccountManager accountManager;

    @Bean
    public RPCService newRPCService() {
        RPCBootstrap rpcBootstrap = new RPCBootstrap();
        rpcBootstrap.setConfig(rpcConfig);
        rpcBootstrap.setAccountManager(accountManager);
        rpcBootstrap.setUserContext(userContext);
        rpcBootstrap.setAuthFilter(authFilter);

        URIHandlerDispatcher uriHandlerDispatcher = new URIHandlerDispatcher();
        rpcBootstrap.setUriHandlerDispatcher(uriHandlerDispatcher);

        RPCService rpcService = new RPCService();
        rpcService.setRpcBootstrap(rpcBootstrap);
        return rpcService;
    }
}
