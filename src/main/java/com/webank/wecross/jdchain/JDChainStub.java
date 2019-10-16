package com.webank.wecross.jdchain;

import com.jd.blockchain.crypto.HashDigest;
import com.jd.blockchain.ledger.BlockchainKeypair;
import com.jd.blockchain.sdk.BlockchainService;
import com.webank.wecross.jdchain.config.JDChainSdk;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.State;
import com.webank.wecross.stub.Stub;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDChainStub implements Stub {

    private Boolean isInit = false;
    private String pattern;
    private BlockchainKeypair adminKey;
    private HashDigest ledgerHash;
    private List<BlockchainService> blockchainService = new ArrayList<BlockchainService>();
    private Map<String, JDChainResource> resources;

    private Logger logger = LoggerFactory.getLogger(JDChainSdk.class);

    public Map<String, JDChainResource> getResources() {
        return resources;
    }

    public void setResources(Map<String, JDChainResource> resources) {
        this.resources = resources;
    }

    public Boolean getIsInit() {
        return isInit;
    }

    public void setIsInit(Boolean isInit) {
        this.isInit = isInit;
    }

    public BlockchainKeypair getAdminKey() {
        return adminKey;
    }

    public void setAdminKey(BlockchainKeypair adminKey) {
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

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public void init() throws Exception {}

    @Override
    public String getPattern() {
        return null;
    }

    @Override
    public State getState() {
        return null;
    }

    @Override
    public BlockHeader getBlockHeader() {
        return null;
    }

    @Override
    public Resource getResource(Path path) throws Exception {
        logger.trace("get resource: {}", path.getResource());
        JDChainResource resource = resources.get(path.getResource());
        if (resource != null) {
            resource.init(adminKey, ledgerHash, blockchainService);
            return resource;
        }
        return resource;
    }
}
