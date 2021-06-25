package com.webank.wecross.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.account.RouterLoginAccountContext;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.network.client.ClientMessageEngine;
import com.webank.wecross.utils.ConfigUtils;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminContextConfig {
    @Resource Toml toml;

    @Resource(name = "newAccountManagerEngine")
    ClientMessageEngine accountManagerEngine;

    @Bean
    public RouterLoginAccountContext newAdminContext() throws WeCrossException {
        RouterLoginAccountContext adminContext = new RouterLoginAccountContext();
        String admin = ConfigUtils.parseString(toml, "account-manager.admin");

        adminContext.setAccountManagerEngine(accountManagerEngine);
        adminContext.setUsername(admin);
        adminContext.routerLogin();

        return adminContext;
    }
}
