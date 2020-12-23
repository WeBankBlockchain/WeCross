package com.webank.wecross.config;

import com.webank.wecross.network.client.ClientMessageEngine;
import com.webank.wecross.network.rpc.authentication.AuthFilter;
import com.webank.wecross.network.rpc.authentication.RemoteAuthFilter;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthFilterConfig {
    @Resource(name = "newAccountManagerEngine")
    ClientMessageEngine accountManagerEngine;

    @Bean
    public AuthFilter newAuthFilter() {
        RemoteAuthFilter remoteAuthFilter = new RemoteAuthFilter();
        remoteAuthFilter.setRemoteEngine(accountManagerEngine);
        remoteAuthFilter.registerAuthUri("/auth/register");
        remoteAuthFilter.registerAuthUri("/auth/login");
        remoteAuthFilter.registerAuthUri("/auth/logout");
        remoteAuthFilter.registerAuthUri("/auth/addChainAccount");
        remoteAuthFilter.registerAuthUri("/auth/removeChainAccount");
        remoteAuthFilter.registerAuthUri("/auth/setDefaultAccount");
        remoteAuthFilter.registerAuthUri("/auth/listAccount");
        remoteAuthFilter.registerAuthUri("/auth/authCode");
        remoteAuthFilter.registerAuthUri("/auth/pub");
        remoteAuthFilter.registerAuthUri("/auth/changePassword");

        return remoteAuthFilter;
    }
}
