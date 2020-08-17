package com.webank.wecross.config;

import com.webank.wecross.routine.xa.XATransactionManager;
import com.webank.wecross.zone.ZoneManager;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XATransactionManagerConfig {
    @Resource private ZoneManager zoneManager;

    @Bean
    public XATransactionManager getXATransactionManager() {
        XATransactionManager xaTransactionManager = new XATransactionManager();
        xaTransactionManager.setZoneManager(zoneManager);

        return xaTransactionManager;
    }
}
