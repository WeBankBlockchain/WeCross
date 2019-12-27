package com.webank.wecross.stub;

import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;

public interface Stub {

    public String getType();

    public ChainState getChainState();

    public void updateChainstate();

    default Resource getResource(Path path) throws Exception {
        getLogger().trace("get resource: {}", path.getResource());
        return getResources().get(path.getResource());
    }

    default void addResource(Resource resource) throws Exception {
        String name = resource.getPath().getResource();
        Resource currentResource = getResources().get(name);
        if (currentResource == null) {
            getResources().put(name, resource);
        } else {
            if (currentResource.getDistance() > resource.getDistance()) {
                getResources().put(name, resource); // Update to shorter path resource
            }
        }
    }

    default void removeResource(Path path, boolean ignoreLocal) throws Exception {
        Resource resource = getResource(path);
        if (ignoreLocal && resource != null && resource.getDistance() == 0) {
            getLogger().trace("remove resource ignore local resources: {}", path.getResource());
            return;
        }

        getLogger().info("remove resource: {}", path.getResource());

        if (getResources().containsKey(path.getResource())) {
            getResources().remove(path.getResource());
        }
    }

    public Map<String, Resource> getResources();

    default Set<String> getAllResourceName(boolean ignoreRemote) {
        Set<String> names = new HashSet<>();
        if (getResources() == null) {
            return names;
        }

        for (Resource resource : getResources().values()) {
            if (resource.getDistance() == 0 || !ignoreRemote) {
                names.add(resource.getPath().getResource());
            }
        }
        return names;
    }

    Logger getLogger();
}
