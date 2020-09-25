package com.webank.wecross.config;

import com.webank.wecross.account.AccountSyncManager;
import com.webank.wecross.stubmanager.StubManager;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountSyncManagerConfig {
    @Resource StubManager stubManager;

    @Bean
    public AccountSyncManager newAccountSyncManager() {
        AccountSyncManager accountSyncManager = new AccountSyncManager();
        accountSyncManager.setStubManager(stubManager);
        return accountSyncManager;
    }
}
