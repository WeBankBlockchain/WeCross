package com.webank.wecross.config;

import com.webank.wecross.network.rpc.web.WebService;
import com.webank.wecross.network.rpc.web.WebURIHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebServiceConfig {
    @Bean
    public WebService newWebService() {
        WebURIHandler handler = new WebURIHandler();

        WebService webService = new WebService();
        webService.setHandler(handler);
        return webService;
    }
}
