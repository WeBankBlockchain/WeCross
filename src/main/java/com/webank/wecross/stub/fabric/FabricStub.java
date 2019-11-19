package com.webank.wecross.stub.fabric;

import com.webank.wecross.network.config.ConfigType;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.ChainState;
import com.webank.wecross.stub.Stub;
import com.webank.wecross.stub.jdchain.config.JDChainSdk;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabricStub implements Stub {

    private Logger logger = LoggerFactory.getLogger(JDChainSdk.class);

    private HFClient hfClient = null;
    private Channel channel = null;
    private Map<String, Resource> resources = new HashMap<String, Resource>();
    private ChainState chainState;

    @Override
    public ChainState getChainState() {
        return chainState;
    }

    @Override
    public String getType() {
        return ConfigType.STUB_TYPE_FABRIC;
    }

    @Override
    public void updateChainstate() {}

    @Override
    public Resource getResource(Path path) throws Exception {
        logger.trace("get resource: {}", path.getResource());
        Resource resource = resources.get(path.getResource());
        if (resource != null && resource.getDistance() == 0) {
            ((FabricContractResource) resource).init(hfClient, channel);
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
    public Map<String, Resource> getResources() {
        return resources;
    }

    @Override
    public Set<String> getAllResourceName(boolean ignoreRemote) {
        Set<String> names = new HashSet<>();
        if (resources == null) {
            return names;
        }

        for (Resource resource : resources.values()) {
            if (resource.getDistance() == 0 || !ignoreRemote) {
                names.add(resource.getPath().getResource());
            }
        }
        return names;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public HFClient getHfClient() {
        return hfClient;
    }

    public void setHfClient(HFClient hfClient) {
        this.hfClient = hfClient;
    }

    public void setResources(Map<String, Resource> resources) {
        this.resources = resources;
    }
}
