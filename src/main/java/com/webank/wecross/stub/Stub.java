package com.webank.wecross.stub;

import com.webank.wecross.resource.Resource;
import com.webank.wecross.resource.URI;

public interface Stub {
    public void init() throws Exception;

    public String getPattern();

    public State getState();

    public BlockHeader getBlockHeader();

    public Resource getResource(URI uri) throws Exception;
}
