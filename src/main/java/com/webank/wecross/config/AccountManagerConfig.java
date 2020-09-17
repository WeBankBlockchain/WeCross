package com.webank.wecross.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.account.AccountManager;
import com.webank.wecross.account.AdminContext;
import com.webank.wecross.account.UserContext;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.network.client.ClientMessageEngine;
import com.webank.wecross.network.p2p.netty.factory.P2PConfig;
import com.webank.wecross.stubmanager.StubManager;
import java.io.IOException;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountManagerConfig {
    @Resource Toml toml;

    @Resource StubManager stubManager;

    @Resource P2PConfig weCrossConfig;

    @Resource(name = "newUserContext")
    UserContext userContext;

    @Resource AdminContext adminContext;

    @Resource(name = "newAccountManagerEngine")
    ClientMessageEngine accountManagerEngine;

    private Logger logger = LoggerFactory.getLogger(AccountManagerConfig.class);

    @Bean
    public AccountManager newAccountManager() throws IOException, WeCrossException {

        AccountManager accountManager = new AccountManager();
        // accountManager.setStubManager(stubManager);
        accountManager.setEngine(accountManagerEngine);
        accountManager.setAdminContext(adminContext);

        /*
            AccountManager localAccountManager = newLocalAccountManager();
            userContext.setToken(WeCrossDefault.EMPTY_TOKEN);
            remoteAccountManager.setAccounts(
                    localAccountManager.getAccounts()); // set according with rpcContext
        */
        return accountManager;
    }

    @Bean
    public UserContext newUserContext() throws WeCrossException {
        return new UserContext();
    }
}
