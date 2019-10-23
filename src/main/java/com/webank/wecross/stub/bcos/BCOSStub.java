package com.webank.wecross.stub.bcos;

import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.ChainState;
import com.webank.wecross.stub.Stub;
import java.util.HashSet;
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

    private Map<String, Resource> resources;

    private ChainState chainState;

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
    public ChainState getChainState() {
        return chainState;
    }

    @Override
    public void updateChainstate() {
        // get state from chain and update chainState
    }

    @Override
    public Resource getResource(Path path) throws Exception {
        logger.trace("get resource: {}", path.getResource());

        Resource resource = resources.get(path.getResource());

        if (resource != null && resource.getDistance() == 0) {
            ((BCOSResource) resource).init(bcosService, web3, credentials);
            return resource;
        }
        return resource;
    }

    @Override
    public void addResource(Resource resource) throws Exception {
        String name = resource.getPath().getResource();
        Resource currentResource = resources.get(name);
        if (currentResource == null) {
            resources.put(name, resource);
        } else {
            if (currentResource.getDistance() > resource.getDistance()) {
                resources.put(name, resource); // Update to shorter path resource
            }
        }
    }

    @Override
    public void removeResource(Path path, boolean ignoreLocal) throws Exception {
        Resource resource = getResource(path);
        if (ignoreLocal && resource != null && resource.getDistance() == 0) {
            logger.trace("remove resource ignore local resources: {}", path.getResource());
            return;
        }
        logger.info("remove resource: {}", path.getResource());
        resources.remove(path.getResource());
    }

    @Override
    public Set<String> getAllResourceName(boolean ignoreRemote) {
        Set<String> names = new HashSet<>();
        for (Map.Entry<String, Resource> entry : resources.entrySet()) {
            if (entry.getValue().getDistance() == 0 || !ignoreRemote) {
                names.add(entry.getKey());
            }
        }
        return names;
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

    @Override
    public Map<String, Resource> getResources() {
        return resources;
    }

    public void setResources(Map<String, Resource> resources) {
        this.resources = resources;
    }

    public void setChainState(ChainState chainState) {
        this.chainState = chainState;
    }
}
