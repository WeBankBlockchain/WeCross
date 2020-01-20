package com.webank.wecross.stub.remote;

import com.webank.wecross.common.WeCrossType;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.ChainState;
import com.webank.wecross.stub.Stub;
import java.util.HashMap;
import java.util.Map;
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
        return WeCrossType.STUB_TYPE_REMOTE;
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
    public Map<String, Resource> getResources() {
        return resources;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    public void setResources(Map<String, Resource> resources) {
        this.resources = resources;
    }
}
