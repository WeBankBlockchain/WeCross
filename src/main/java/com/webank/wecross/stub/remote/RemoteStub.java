package com.webank.wecross.stub.remote;

import com.webank.wecross.common.WeCrossType;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.Stub;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteStub implements Stub {

    private Logger logger = LoggerFactory.getLogger(RemoteStub.class);
    private Map<String, Resource> resources;
    private String path;

    public RemoteStub() {
        this.resources = new HashMap<>();
    }

    @Override
    public String getType() {
        return WeCrossType.STUB_TYPE_REMOTE;
    }

    @Override
    public int getBlockNumber() {
        return 0;
    }

    @Override
    public BlockHeader getBlockHeader(int blockNumber) {
        return null;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
