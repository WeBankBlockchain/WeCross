package com.webank.wecross.stub.bcos.config;

import com.webank.wecross.config.ConfigUtils;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Web3jFactory {

    private static Logger logger = LoggerFactory.getLogger(ConfigUtils.class);

    private Service bcosService;

    public Web3jFactory(Service bcosService) {
        this.bcosService = bcosService;
    }

    public Web3j getWeb3j(Integer timeout) {
        ChannelEthereumService channelEthereumService = new ChannelEthereumService();

        Runnable runnable =
                new Runnable() {
                    public void run() {
                        try {
                            bcosService.run();
                        } catch (Exception e) {
                            logger.warn(
                                    "Something wrong with running bcos server: {}", e.getMessage());
                        }
                    }
                };

        Thread thread = new Thread(runnable);
        thread.start();

        channelEthereumService.setChannelService(bcosService);
        channelEthereumService.setTimeout(timeout);

        return Web3j.build(channelEthereumService, bcosService.getGroupId());
    }

    public Service getBcosService() {
        return bcosService;
    }

    public void setBcosService(Service bcosService) {
        this.bcosService = bcosService;
    }
}
