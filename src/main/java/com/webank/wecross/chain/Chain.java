package com.webank.wecross.chain;

import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.BlockHeader;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Chain {
    private Logger logger = LoggerFactory.getLogger(Chain.class);
    private Map<String, Resource> resources = new HashMap<String, Resource>();
    private String path;

    public int getBlockNumber() {
        return 0;
    }

    public BlockHeader getBlockHeader(int blockNumber) {
        return null;
    }

    public Map<String, Resource> getResources() {
        return resources;
    }

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
