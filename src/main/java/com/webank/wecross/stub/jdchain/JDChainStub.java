package com.webank.wecross.stub.jdchain;

import com.jd.blockchain.crypto.HashDigest;
import com.jd.blockchain.ledger.BlockchainKeypair;
import com.jd.blockchain.sdk.BlockchainService;
import com.webank.wecross.chain.BlockHeader;
import com.webank.wecross.chain.Chain;
import com.webank.wecross.common.WeCrossType;
import com.webank.wecross.resource.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDChainStub implements Chain {

    private List<BlockchainKeypair> adminKey = new ArrayList<BlockchainKeypair>();
    private HashDigest ledgerHash;
    private List<BlockchainService> blockchainService = new ArrayList<BlockchainService>();
    private Map<String, Resource> resources = new HashMap<String, Resource>();

    private Logger logger = LoggerFactory.getLogger(JDChainSdk.class);

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

    public List<BlockchainKeypair> getAdminKey() {
        return adminKey;
    }

    public void setAdminKey(List<BlockchainKeypair> adminKey) {
        this.adminKey = adminKey;
    }

    public HashDigest getLedgerHash() {
        return ledgerHash;
    }

    public void setLedgerHash(HashDigest ledgerHash) {
        this.ledgerHash = ledgerHash;
    }

    public List<BlockchainService> getBlockchainService() {
        return blockchainService;
    }

    public void setBlockchainService(List<BlockchainService> blockchainService) {
        this.blockchainService = blockchainService;
    }

    @Override
    public String getType() {
        return WeCrossType.STUB_TYPE_JDCHAIN;
    }
}
