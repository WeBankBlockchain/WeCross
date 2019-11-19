package com.webank.wecross.stub.jdchain;

import com.jd.blockchain.crypto.HashDigest;
import com.jd.blockchain.ledger.BlockchainKeypair;
import com.jd.blockchain.sdk.BlockchainService;
import com.webank.wecross.config.ConfigInfo;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.ChainState;
import com.webank.wecross.stub.Stub;
import com.webank.wecross.stub.jdchain.config.JDChainSdk;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDChainStub implements Stub {

    private List<BlockchainKeypair> adminKey = new ArrayList<BlockchainKeypair>();
    private HashDigest ledgerHash;
    private List<BlockchainService> blockchainService = new ArrayList<BlockchainService>();
    private Map<String, Resource> resources = new HashMap<String, Resource>();
    private ChainState chainState;

    private Logger logger = LoggerFactory.getLogger(JDChainSdk.class);

    @Override
    public Map<String, Resource> getResources() {
        return resources;
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
        return ConfigInfo.STUB_TYPE_JDCHAIN;
    }

    @Override
    public Resource getResource(Path path) throws Exception {
        logger.trace("get resource: {}", path.getResource());
        Resource resource = resources.get(path.getResource());
        if (resource != null && resource.getDistance() == 0) {
            ((JDChainResource) resource).init(adminKey, ledgerHash, blockchainService);
            return resource;
        }
        return resource;
    }

    @Override
    public void addResource(Resource resource) throws Exception {
        String name = resource.getPath().getResource();
        Resource currentResource = resources.get(name);
        if (currentResource == null) {
            resources.put(name, resource);
        } else {
            if (currentResource.getDistance() > resource.getDistance()) {
                resources.put(name, resource); // Update to shorter path resource
            }
        }
    }

    @Override
    public void removeResource(Path path, boolean ignoreLocal) throws Exception {
        Resource resource = getResource(path);
        if (ignoreLocal && resource != null && resource.getDistance() == 0) {
            logger.trace("remove resource ignore local resources: {}", path.getResource());
            return;
        }

        logger.info("remove resource: {}", path.getResource());
        resources.remove(path.getResource());
    }

    @Override
    public ChainState getChainState() {
        return chainState;
    }

    @Override
    public void updateChainstate() {
        // get state from chain and update chainState
    }

    @Override
    public Set<String> getAllResourceName(boolean ignoreRemote) {
        Set<String> names = new HashSet<>();
        if (resources == null) {
            return names;
        }

        for (Resource resource : resources.values()) {
            if (resource.getDistance() == 0 || !ignoreRemote) {
                names.add(resource.getPath().getResource());
            }
        }
        return names;
    }

    public void setChainState(ChainState chainState) {
        this.chainState = chainState;
    }
}
