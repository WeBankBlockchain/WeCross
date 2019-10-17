package com.webank.wecross.stub;

import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import java.util.Set;

public interface Stub {
    public void init() throws Exception;

    public String getPattern();

    public ChainState getState();

    public Resource getResource(Path path) throws Exception;

    public Set<String> getAllResourceName();
}
