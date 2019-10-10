package com.webank.wecross.bcos;

import com.webank.wecross.resource.Resource;
import com.webank.wecross.resource.URI;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.State;
import com.webank.wecross.stub.Stub;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.fisco.bcos.web3j.utils.Web3AsyncThreadPoolSize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BCOSStub implements Stub {
    private Boolean isInit = false;
    private String pattern;
    private Service bcosService;
    private Web3j web3;
    private Credentials credentials;
    private Map<String, BCOSResource> resources;

    private Logger logger = LoggerFactory.getLogger(BCOSStub.class);

    @Override
    public void init() throws Exception {
        if (!isInit) {
            ChannelEthereumService channelEthereumService = new ChannelEthereumService();
            channelEthereumService.setChannelService(bcosService);

            Web3AsyncThreadPoolSize.web3AsyncCorePoolSize = 30;
            Web3AsyncThreadPoolSize.web3AsyncPoolSize = 20;

            ScheduledExecutorService scheduledExecutorService =
                    Executors.newScheduledThreadPool(50);
            setWeb3(Web3j.build(channelEthereumService, 15 * 100, scheduledExecutorService, 1));

            credentials =
                    Credentials.create(
                            "00000000000000000000000000000000000000000000000000000000000000");

            bcosService.run();
            logger.info("BCOS Service start ok!");

            isInit = true;
        }
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public State getState() {
        return null;
    }

    @Override
    public BlockHeader getBlockHeader() {
        return null;
    }

    @Override
    public Resource getResource(URI uri) throws Exception {
        logger.trace("get resource: {}", uri.getResource());

        BCOSResource resource = resources.get(uri.getResource());

        if (resource != null) {
            resource.setWeb3(web3);
            resource.setBcosService(bcosService);
            resource.init(bcosService, web3, credentials);

            return resource;
        }

        return resource;
    }

    public Service getBcosService() {
        return bcosService;
    }

    public void setBcosService(Service bcosService) {
        this.bcosService = bcosService;
    }

    public Web3j getWeb3() {
        return web3;
    }

    public void setWeb3(Web3j web3) {
        this.web3 = web3;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public Map<String, BCOSResource> getResources() {
        return resources;
    }

    public void setResources(Map<String, BCOSResource> resources) {
        this.resources = resources;
    }
}
