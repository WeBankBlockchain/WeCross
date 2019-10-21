package com.webank.wecross.stub;

import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import java.util.Map;
import java.util.Set;

public interface Stub {
    public void init() throws Exception;

    public String getPattern();

    public ChainState getChainState();

    public void updateChainstate();

    public Resource getResource(Path path) throws Exception;

    public void addResource(Resource resource) throws Exception;

    public void removeResource(Path path) throws Exception;

    public Map<String, Resource> getResources();

    public Set<String> getAllResourceName(boolean ignoreRemote);
}
