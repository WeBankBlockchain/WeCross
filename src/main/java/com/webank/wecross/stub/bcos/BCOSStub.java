package com.webank.wecross.stub.bcos;

import com.webank.wecross.chain.BlockHeader;
import com.webank.wecross.chain.Chain;
import com.webank.wecross.common.WeCrossType;
import com.webank.wecross.resource.Resource;

import java.util.Map;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BCOSStub implements Chain {
    private Service bcosService;

    private Web3j web3;

    private Credentials credentials;

    private Map<String, Resource> resources;

    private Logger logger = LoggerFactory.getLogger(BCOSStub.class);

    @Override
    public String getType() {
        return WeCrossType.STUB_TYPE_BCOS;
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

    public Service getBcosService() {
        return bcosService;
    }

    public void setBcosService(Service bcosService) {
        this.bcosService = bcosService;
    }

    public Web3j getWeb3() {
        return web3;
    }

    public void setWeb3(Web3j web3) {
        this.web3 = web3;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
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
