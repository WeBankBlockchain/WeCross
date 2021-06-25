package com.webank.wecross.config;

import com.webank.wecross.account.AccountManager;
import com.webank.wecross.account.AccountSyncManager;
import com.webank.wecross.account.RouterLoginAccountContext;
import com.webank.wecross.account.UniversalAccountFactory;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.network.client.ClientConnection;
import com.webank.wecross.network.client.ClientMessageEngine;
import java.io.IOException;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountManagerConfig {

    @Resource RouterLoginAccountContext adminContext;

    @Resource(name = "newAccountManagerEngine")
    ClientMessageEngine accountManagerEngine;

    @Resource UniversalAccountFactory universalAccountFactory;

    @Resource AccountSyncManager accountSyncManager;

    @Resource(name = "newClientConnection")
    ClientConnection clientConnection;

    @Bean
    public AccountManager newAccountManager() throws IOException, WeCrossException {

        AccountManager accountManager = new AccountManager();
        accountManager.setEngine(accountManagerEngine);
        accountManager.setAdminContext(adminContext);
        accountManager.setUniversalAccountFactory(universalAccountFactory);
        accountManager.setAccountSyncManager(accountSyncManager);
        accountManager.setClientConnection(clientConnection);

        /*
            AccountManager localAccountManager = newLocalAccountManager();
            userContext.setToken(WeCrossDefault.EMPTY_TOKEN);
            remoteAccountManager.setAccounts(
                    localAccountManager.getAccounts()); // set according with rpcContext
        */
        accountManager.start();
        return accountManager;
    }
}
