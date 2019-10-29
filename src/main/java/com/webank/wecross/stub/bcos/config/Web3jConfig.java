package com.webank.wecross.stub.bcos.config;

import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;

public class Web3jConfig {

    private Service bcosService;

    public Web3jConfig(Service bcosService) {
        this.bcosService = bcosService;
    }

    public Web3j getWeb3j() throws Exception {
        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        bcosService.run();
        channelEthereumService.setChannelService(bcosService);

        return Web3j.build(channelEthereumService, bcosService.getGroupId());
    }

    public Service getBcosService() {
        return bcosService;
    }

    public void setBcosService(Service bcosService) {
        this.bcosService = bcosService;
    }
}
