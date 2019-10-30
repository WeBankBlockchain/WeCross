package com.webank.wecross.stub.remote;

import com.webank.wecross.network.config.ConfigType;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.ChainState;
import com.webank.wecross.stub.Stub;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteStub implements Stub {

    private Logger logger = LoggerFactory.getLogger(RemoteStub.class);
    private ChainState chainState;
    private Map<String, Resource> resources;

    public RemoteStub() {
        this.chainState = new ChainState();
        this.resources = new HashMap<>();
    }

    @Override
    public String getType() {
        return ConfigType.STUB_TYPE_REMOTE;
    }

    @Override
    public ChainState getChainState() {
        return chainState;
    }

    @Override
    public void updateChainstate() {
        // query state from peer and update chainState
    }

    @Override
    public Resource getResource(Path path) throws Exception {
        return resources.get(path.getResource());
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

        if (resources.containsKey(path.getResource())) {
            resources.remove(path.getResource());
        }
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

    @Override
    public Map<String, Resource> getResources() {
        return resources;
    }

    public void setResources(Map<String, Resource> resources) {
        this.resources = resources;
    }
}
