package com.webank.wecross.stub;

import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;

public interface Stub {
    public void init() throws Exception;

    public String getPattern();

    public State getState();

    public BlockHeader getBlockHeader();

    public Resource getResource(Path path) throws Exception;
}
