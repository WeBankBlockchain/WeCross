package com.webank.wecross.config;

import com.webank.wecross.account.AccountManager;
import com.webank.wecross.interchain.InterchainManager;
import com.webank.wecross.zone.ZoneManager;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InterchainManagerConfig {

    @Resource private AccountManager accountManager;

    @Resource private ZoneManager zoneManager;

    @Bean
    public InterchainManager newInterchainManager() {
        System.out.println("Initializing InterchainManager ...");

        InterchainManager interchainManager = new InterchainManager();
        interchainManager.setAccountManager(accountManager);
        interchainManager.setZoneManager(zoneManager);
        return interchainManager;
    }
}
