package com.webank.wecross.stub.fabric;

import com.webank.wecross.common.WeCrossType;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.Stub;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabricStub implements Stub {

    private Logger logger = LoggerFactory.getLogger(FabricStub.class);

    private Map<String, FabricConn> fabricConns = new HashMap<String, FabricConn>();
    private Map<String, Resource> resources = new HashMap<String, Resource>();

    @Override
    public String getType() {
        return WeCrossType.STUB_TYPE_FABRIC;
    }

    @Override
    public int getBlockNumber() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public BlockHeader getBlockHeader(int blockNumber) {
        // TODO Auto-generated method stub
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

    public Map<String, FabricConn> getFabricConns() {
        return fabricConns;
    }

    public void setFabricConns(Map<String, FabricConn> fabricConns) {
        this.fabricConns = fabricConns;
    }
}
