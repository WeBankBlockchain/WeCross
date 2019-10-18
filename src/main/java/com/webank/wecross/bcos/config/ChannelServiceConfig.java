package com.webank.wecross.bcos.config;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.handler.GroupChannelConnectionsConfig;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "bcos-channel-service-manager")
public class ChannelServiceConfig {

    private Logger logger = LoggerFactory.getLogger(ChannelServiceConfig.class);

    private Map<String, ChannelService> channelServices;

    @Resource private Credentials credentials;

    @Bean
    public Map<String, Web3Sdk> getConfigurations() {
        Map<String, Web3Sdk> result = new HashMap<>();
        if (channelServices == null) {
            return result;
        }
        for (String channelName : channelServices.keySet()) {
            Web3Sdk bcosConfiguration = new Web3Sdk();

            ChannelService channelService = channelServices.get(channelName);

            GroupChannelConnections groupChannelConnections =
                    channelService.getGroupChannelConnections();

            // init GroupChannelConnectionsConfig
            GroupChannelConnectionsConfig groupChannelConnectionsConfig =
                    groupChannelConnections.getGroupChannelConnections();

            // init Service
            Service bcosService = channelService.getService(groupChannelConnectionsConfig);

            // init Web3j
            Web3jConfig web3jConfig = new Web3jConfig(bcosService);
            try {
                Web3j web3j = web3jConfig.getWeb3j();
                bcosConfiguration.setBcosService(bcosService);
                bcosConfiguration.setCredentials(credentials);
                bcosConfiguration.setWeb3(web3j);
            } catch (Exception e) {
                logger.error(e.toString());
            }

            result.put(channelName, bcosConfiguration);
        }
        return result;
    }

    public Map<String, ChannelService> getChannelServices() {
        return channelServices;
    }

    public void setChannelServices(Map<String, ChannelService> channelServices) {
        this.channelServices = channelServices;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }
}
