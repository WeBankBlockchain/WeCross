package com.webank.wecross.bcos.config;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.fisco.bcos.web3j.utils.Web3AsyncThreadPoolSize;

public class Web3jConfig {

    private Service bcosService;

    public Web3jConfig(Service bcosService) {
        this.bcosService = bcosService;
    }

    public Web3j getWeb3j() throws Exception {
        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        bcosService.run();
        channelEthereumService.setChannelService(bcosService);

        Web3AsyncThreadPoolSize.web3AsyncCorePoolSize = 30;
        Web3AsyncThreadPoolSize.web3AsyncPoolSize = 20;

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(50);

        return Web3j.build(
                channelEthereumService,
                15 * 100,
                scheduledExecutorService,
                bcosService.getGroupId());
    }

    public Service getBcosService() {
        return bcosService;
    }

    public void setBcosService(Service bcosService) {
        this.bcosService = bcosService;
    }
}
