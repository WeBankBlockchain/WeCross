package com.webank.wecross.stub.jdchain.config;

import com.jd.blockchain.crypto.HashDigest;
import com.jd.blockchain.ledger.BlockchainKeypair;
import com.jd.blockchain.sdk.BlockchainService;
import java.util.ArrayList;
import java.util.List;

public class JDChainSdk {

    private BlockchainKeypair adminKey;
    private HashDigest ledgerHash;
    private List<BlockchainService> blockchainService = new ArrayList<BlockchainService>();

    public List<BlockchainService> getBlockchainService() {
        return blockchainService;
    }

    public void setBlockchainService(List<BlockchainService> blockchainService) {
        this.blockchainService = blockchainService;
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
}
