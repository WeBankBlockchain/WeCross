package com.webank.wecross.config;

import com.webank.wecross.account.AccountManager;
import com.webank.wecross.routine.xa.XATransactionManager;
import com.webank.wecross.zone.ZoneManager;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XATransactionManagerConfig {
    @Resource private ZoneManager zoneManager;
    @Resource private AccountManager accountManager;

    @Bean
    public XATransactionManager getXATransactionManager() {
        XATransactionManager xaTransactionManager = new XATransactionManager();
        xaTransactionManager.setZoneManager(zoneManager);
        xaTransactionManager.setAccountManager(accountManager);

        return xaTransactionManager;
    }
}
