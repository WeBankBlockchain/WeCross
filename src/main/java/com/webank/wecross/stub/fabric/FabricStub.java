package com.webank.wecross.stub.fabric;

import com.webank.wecross.config.ConfigInfo;
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

public class FabricStub implements Stub {

    private Logger logger = LoggerFactory.getLogger(FabricStub.class);

    private Map<String, FabricConn> fabricConns = new HashMap<String, FabricConn>();
    private Map<String, Resource> resources = new HashMap<String, Resource>();
    private ChainState chainState;

    @Override
    public ChainState getChainState() {
        return chainState;
    }

    @Override
    public String getType() {
        return ConfigInfo.STUB_TYPE_FABRIC;
    }

    @Override
    public void updateChainstate() {}

    @Override
    public Resource getResource(Path path) throws Exception {

        logger.trace("get resource: {}", path.getResource());
        Resource resource = resources.get(path.getResource());
        if (resource != null && resource.getDistance() == 0) {
            String pathStr = path.getResource();
            String[] pathlist = pathStr.split("\\.");
            String name = pathlist[pathlist.length - 1];
            FabricConn fabricConn = fabricConns.get(name);
            if (fabricConn == null) {
                logger.error("path:{} name:{} not exist in fabricConns", pathStr, name);
                return null;
            }
            ((FabricContractResource) resource).init(fabricConn);
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

    public void setResources(Map<String, Resource> resources) {
        this.resources = resources;
    }

    public Map<String, FabricConn> getFabricConns() {
        return fabricConns;
    }

    public void setFabricConns(Map<String, FabricConn> fabricConns) {
        this.fabricConns = fabricConns;
    }
}
