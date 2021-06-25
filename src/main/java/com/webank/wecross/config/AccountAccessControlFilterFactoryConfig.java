package com.webank.wecross.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.account.AccountAccessControlFilterFactory;
import com.webank.wecross.utils.ConfigUtils;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountAccessControlFilterFactoryConfig {
    @Resource Toml toml;

    @Bean
    public AccountAccessControlFilterFactory newAccountAccessControlFilterFactory() {
        boolean enable = ConfigUtils.parseBoolean(toml, "common.enableAccessControl", false);
        AccountAccessControlFilterFactory factory = new AccountAccessControlFilterFactory();
        factory.setEnableAccessControl(enable);
        return factory;
    }
}
