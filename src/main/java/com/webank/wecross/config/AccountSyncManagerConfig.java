package com.webank.wecross.config;

import com.webank.wecross.account.AccountSyncManager;
import com.webank.wecross.account.uaproof.UAProofGenerator;
import com.webank.wecross.account.uaproof.UAProofVerifier;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountSyncManagerConfig {
    @Resource UAProofVerifier uaProofVerifier;

    @Resource UAProofGenerator uaProofGenerator;

    @Bean
    public AccountSyncManager newAccountSyncManager() {
        AccountSyncManager accountSyncManager = new AccountSyncManager();
        accountSyncManager.setUaProofVerifier(uaProofVerifier);
        accountSyncManager.setUaProofGenerator(uaProofGenerator);
        return accountSyncManager;
    }
}
