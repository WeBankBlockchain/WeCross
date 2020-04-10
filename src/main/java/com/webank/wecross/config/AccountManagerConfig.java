package com.webank.wecross.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.account.AccountManager;
import com.webank.wecross.common.WeCrossDefault;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.stub.Account;
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
    @Resource Toml toml;

    @Resource StubManager stubManager;

    @Resource P2PConfig weCrossConfig;

    private Logger logger = LoggerFactory.getLogger(AccountManagerConfig.class);

    @Bean
    public AccountManager newAccountManager() throws IOException, WeCrossException {
        String accountsPath = toml.getString("accounts.path", "classpath:accounts/");

        System.out.println("Initializing AccountManager with config(" + accountsPath + ") ...");

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
                                    "file:"
                                            + resource.getFile().getAbsolutePath()
                                            + "/account.toml");
                    Toml toml = new Toml();
                    toml.read(accountConfig.getInputStream());

                    String type = toml.getString("account.type");

                    logger.debug(
                            "Loading account, path: {}, name: {}, type: {}",
                            accountConfig.getURI(),
                            resource.getFile().getName(),
                            type);

                    if (type == null) {
                        logger.error(
                                "Could not load account type. path: {}", accountConfig.getURI());
                        continue;
                    }

                    if (!stubManager.hasDriver(type)) {
                        logger.error("Stub plugin[" + type + "] not found!");
                        continue;
                    }

                    Account account =
                            stubManager
                                    .getStubFactory(type)
                                    .newAccount(
                                            resource.getFilename(),
                                            resource.getFile().getAbsolutePath());
                    if (account == null) {
                        logger.error(
                                "Load account path: {}, name: {} type: {} failed",
                                accountConfig.getURI(),
                                resource.getFile().getName(),
                                type);
                        continue;
                    }
                    accountManager.addAccount(resource.getFile().getName(), account);
                }
            }

            return accountManager;
        } catch (IOException e) {
            throw e;
        }
    }
}
