package com.webank.wecross.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.account.Account;
import com.webank.wecross.account.AccountManager;
import com.webank.wecross.common.WeCrossDefault;
import com.webank.wecross.stub.StubManager;
import java.io.IOException;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Configuration
public class AccountManagerConfig {
    @Resource(name = "newToml")
    Toml toml;

    @Resource StubManager stubManager;

    @Resource WeCrossConfig weCrossConfig;

    private Logger logger = LoggerFactory.getLogger(AccountManagerConfig.class);

    @Bean
    public AccountManager newAccountManager() throws IOException {
        String accountsPath = toml.getString("accounts.path");

        if (accountsPath == null) {
            String errorMessage =
                    "\"path\" in [accounts] not found, please check "
                            + WeCrossDefault.MAIN_CONFIG_FILE;

            logger.error(errorMessage);
            return null;
        }

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            org.springframework.core.io.Resource[] resources =
                    resolver.getResources(accountsPath + "/*");

            AccountManager accountManager = new AccountManager();
            for (org.springframework.core.io.Resource resource : resources) {
                if (resource.getFile().isDirectory()) {
                    org.springframework.core.io.Resource accountConfig =
                            resolver.getResource(
                                    resource.getFile().getAbsolutePath() + "/account.toml");
                    Toml toml = new Toml();
                    toml.read(accountConfig.getInputStream());

                    String type = toml.getString("type");

                    logger.debug(
                            "Loading account, path: {}, name: {}, type: {}",
                            accountConfig.getURI(),
                            resource.getFile().getName(),
                            type);

                    Account account =
                            stubManager
                                    .getStubFactory(type)
                                    .newAccount(resource.getFile().getAbsolutePath());
                    accountManager.addAccount(resource.getFile().getName(), account);
                }
            }

            return accountManager;
        } catch (IOException e) {
            // logger.error("Load accounts error", e);
            throw e;
        }
    }
}
