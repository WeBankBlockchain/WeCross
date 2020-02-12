package com.webank.wecross.stub.jdchain;

import com.jd.blockchain.crypto.HashDigest;
import com.jd.blockchain.ledger.BlockchainKeypair;
import com.jd.blockchain.sdk.BlockchainService;
import java.util.ArrayList;
import java.util.List;

public class JDChainSdk {

    private List<BlockchainKeypair> adminKey = new ArrayList<BlockchainKeypair>();
    private HashDigest ledgerHash;
    private List<BlockchainService> blockchainService = new ArrayList<BlockchainService>();

    public List<BlockchainService> getBlockchainService() {
        return blockchainService;
    }

    public void setBlockchainService(List<BlockchainService> blockchainService) {
        this.blockchainService = blockchainService;
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
}
