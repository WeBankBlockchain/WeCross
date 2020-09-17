package com.webank.wecross.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.network.client.ClientConnection;
import com.webank.wecross.network.client.ClientMessageEngine;
import com.webank.wecross.network.client.NettyAsyncHttpClientEngine;
import com.webank.wecross.utils.ConfigUtils;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountManagerEngineConfig {
    @Resource Toml toml;

    @Bean
    public ClientMessageEngine newAccountManagerEngine() throws WeCrossException {
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
        return engine;
    }
}
