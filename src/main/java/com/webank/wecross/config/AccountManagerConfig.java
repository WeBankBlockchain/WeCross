package com.webank.wecross.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.account.AccountManager;
import com.webank.wecross.account.LocalAccountManager;
import com.webank.wecross.account.RemoteAccountManager;
import com.webank.wecross.common.WeCrossDefault;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.network.client.ClientConnection;
import com.webank.wecross.network.client.NettyAsyncHttpClientEngine;
import com.webank.wecross.network.p2p.netty.factory.P2PConfig;
import com.webank.wecross.restserver.RPCContext;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stubmanager.StubManager;
import com.webank.wecross.utils.ConfigUtils;
import java.io.IOException;
import java.util.Objects;
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

    @Resource RPCContext rpcContext;

    private Logger logger = LoggerFactory.getLogger(AccountManagerConfig.class);

    @Bean
    public AccountManager newAccountManager() throws IOException, WeCrossException {
        boolean enableRemote = ConfigUtils.parseBoolean(toml, "account-manager.enable", false);
        if (!enableRemote) {
            return newLocalAccountManager();
        } else {
            return newRemoteAccountManager();
        }
    }

    public AccountManager newLocalAccountManager() throws IOException, WeCrossException {
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

            AccountManager accountManager = new LocalAccountManager();
            for (org.springframework.core.io.Resource resource : resources) {
                if (resource.getFile().isDirectory()
                        && !Objects.requireNonNull(resource.getFilename()).startsWith(".")) {
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
                        throw new WeCrossException(
                                WeCrossException.ErrorCode.INVALID_ACCOUNT,
                                "Could not load account type. path: " + accountConfig.getURI());
                    }

                    if (!stubManager.hasFactory(type)) {
                        logger.error("Stub plugin[" + type + "] not found!");
                        throw new WeCrossException(
                                WeCrossException.ErrorCode.INVALID_ACCOUNT,
                                "Stub plugin[" + type + "] not found!");
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
                        throw new WeCrossException(
                                WeCrossException.ErrorCode.INVALID_ACCOUNT,
                                "Invalid account configure: " + accountConfig.getURI());
                    }
                    accountManager.addAccount(resource.getFile().getName(), account);
                }
            }

            return accountManager;
        } catch (IOException e) {
            throw e;
        }
    }

    public AccountManager newRemoteAccountManager() throws IOException, WeCrossException {
        ClientConnection clientConnection = new ClientConnection();

        clientConnection.setServer(ConfigUtils.parseString(toml, "account-manager.server"));
        clientConnection.setSSLKey(ConfigUtils.parseString(toml, "account-manager.sslKey"));
        clientConnection.setSSLCert(ConfigUtils.parseString(toml, "account-manager.sslCert"));
        clientConnection.setCaCert(ConfigUtils.parseString(toml, "account-manager.caCert"));
        clientConnection.setMaxTotal(ConfigUtils.parseInt(toml, "account-manager.maxTotal", 200));
        clientConnection.setMaxPerRoute(
                ConfigUtils.parseInt(toml, "account-manager.maxPerRoute", 8));

        NettyAsyncHttpClientEngine engine = new NettyAsyncHttpClientEngine();
        engine.setClientConnection(clientConnection);
        engine.init();

        RemoteAccountManager remoteAccountManager = new RemoteAccountManager();
        remoteAccountManager.setEngine(engine);
        remoteAccountManager.setRpcContext(rpcContext);

        AccountManager localAccountManager = newLocalAccountManager();
        rpcContext.setToken(WeCrossDefault.LOCAL_ACCOUNT_TOKEN);
        remoteAccountManager.setAccounts(
                localAccountManager.getAccounts()); // set according with rpcContext

        return remoteAccountManager;
    }
}
