package com.webank.wecross.bcos;

import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.ChainState;
import com.webank.wecross.stub.Stub;
import java.util.Map;
import java.util.Set;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// @org.springframework.stereotype.Service("BCOSStub")
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
            //            ChannelEthereumService channelEthereumService = new
            // ChannelEthereumService();
            //            channelEthereumService.setChannelService(bcosService);
            //
            //            Web3AsyncThreadPoolSize.web3AsyncCorePoolSize = 30;
            //            Web3AsyncThreadPoolSize.web3AsyncPoolSize = 20;
            //
            //            ScheduledExecutorService scheduledExecutorService =
            //                Executors.newScheduledThreadPool(50);
            //            setWeb3(Web3j.build(channelEthereumService, 15 * 100,
            // scheduledExecutorService, 1));
            //
            //            credentials =
            //                Credentials.create(
            //                    "00000000000000000000000000000000000000000000000000000000000000");
            //
            //            bcosService.run();
            //            logger.info("BCOS Service start ok!");

            isInit = true;
        }
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public ChainState getState() {
        return null;
    }

    @Override
    public Resource getResource(Path path) throws Exception {
        logger.trace("get resource: {}", path.getResource());

        BCOSResource resource = resources.get(path.getResource());

        if (resource != null) {
            resource.init(bcosService, web3, credentials);

            return resource;
        }

        return resource;
    }

    @Override
    public Set<String> getAllResourceName() {
        return resources.keySet();
    }

    public Boolean getInit() {
        return isInit;
    }

    public void setInit(Boolean init) {
        isInit = init;
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
