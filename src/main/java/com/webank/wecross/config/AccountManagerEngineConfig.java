package com.webank.wecross.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.network.client.ClientConnection;
import com.webank.wecross.network.client.ClientMessageEngine;
import com.webank.wecross.network.client.NettyAsyncHttpClientEngine;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountManagerEngineConfig {
    @Resource Toml toml;

    @Resource(name = "newClientConnection")
    ClientConnection clientConnection;

    @Bean
    public ClientMessageEngine newAccountManagerEngine() throws WeCrossException {

        NettyAsyncHttpClientEngine engine = new NettyAsyncHttpClientEngine();
        engine.setClientConnection(clientConnection);
        engine.init();
        return engine;
    }
}
