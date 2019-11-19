package com.webank.wecross.stub.bcos.config;

import com.webank.wecross.exception.Status;
import com.webank.wecross.exception.WeCrossException;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.handler.GroupChannelConnectionsConfig;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Web3SdkFactory {

    private Logger logger = LoggerFactory.getLogger(Web3SdkFactory.class);

    private Credentials credentials;

    private ChannelService channelService;

    public Web3SdkFactory(Credentials credentials, ChannelService channelService) {
        this.credentials = credentials;
        this.channelService = channelService;
    }

    public Web3Sdk getWeb3Sdk(String stubName) throws WeCrossException {
        Web3Sdk web3Sdk = new Web3Sdk();

        GroupChannelConnections groupChannelConnections =
                channelService.getGroupChannelConnections();
        if (groupChannelConnections == null) {
            logger.error("Error in {}: groupChannelConnections configure is wrong", stubName);
            return null;
        }

        // init GroupChannelConnectionsConfig
        GroupChannelConnectionsConfig groupChannelConnectionsConfig =
                groupChannelConnections.getGroupChannelConnectionsConfig();

        // init Service
        Service bcosService = channelService.getService(groupChannelConnectionsConfig);

        // init Web3j
        Web3jFactory web3JFactory = new Web3jFactory(bcosService);
        try {
            Web3j web3j = web3JFactory.getWeb3j(channelService.getTimeout());
            web3Sdk.setBcosService(bcosService);
            web3Sdk.setCredentials(credentials);
            web3Sdk.setWeb3(web3j);
        } catch (Exception e) {
            throw new WeCrossException(Status.INTERNAL_ERROR, e.toString());
        }

        logger.debug("Init web3sdk finished");
        return web3Sdk;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public ChannelService getChannelService() {
        return channelService;
    }

    public void setChannelService(ChannelService channelService) {
        this.channelService = channelService;
    }
}
