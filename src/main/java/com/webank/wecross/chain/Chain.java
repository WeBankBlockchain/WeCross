package com.webank.wecross.chain;

import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import java.util.Map;
import org.slf4j.Logger;

public interface Chain {
    public String getType();

    public int getBlockNumber();

    public BlockHeader getBlockHeader(int blockNumber);

    default Resource getResource(Path path) throws Exception {
        getLogger().trace("get resource: {}", path.getResource());
        return getResources().get(path.getResource());
    }

    /*
    // return if resource exists
    default boolean addResource(Resource resource) throws Exception {
        String name = resource.getPath().getResource();
        Resource currentResource = getResources().get(name);
        if (currentResource == null) {
            getResources().put(name, resource);

            return false;
        } else {
            if (currentResource.getDistance() > resource.getDistance()) {
                getResources().put(name, resource); // Update to shorter path resource
            }

            return true;
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
    */

    public Map<String, Resource> getResources();

    Logger getLogger();
}
