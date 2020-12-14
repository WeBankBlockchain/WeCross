package com.webank.wecross.config;

import com.webank.wecross.network.rpc.netty.RPCConfig;
import com.webank.wecross.network.rpc.web.WebService;
import com.webank.wecross.network.rpc.web.WebURIHandler;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebServiceConfig {
    @Resource RPCConfig rpcConfig;

    @Bean
    public WebService newWebService() {
        WebURIHandler handler = new WebURIHandler();
        handler.setWebRoot(rpcConfig.getWebRoot());
        WebService webService = new WebService();
        webService.setHandler(handler);
        return webService;
    }
}
