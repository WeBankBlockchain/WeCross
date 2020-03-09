package com.webank.wecross.stub.jdchain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jd.blockchain.crypto.HashDigest;
import com.jd.blockchain.ledger.BlockchainKeypair;
import com.jd.blockchain.sdk.BlockchainService;
import com.webank.wecross.peer.Peer;
import com.webank.wecross.resource.EventCallback;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.restserver.request.GetDataRequest;
import com.webank.wecross.restserver.request.ProposalRequest;
import com.webank.wecross.restserver.request.SetDataRequest;
import com.webank.wecross.restserver.request.TransactionRequest;
import com.webank.wecross.restserver.response.GetDataResponse;
import com.webank.wecross.restserver.response.ProposalResponse;
import com.webank.wecross.restserver.response.SetDataResponse;
import com.webank.wecross.restserver.response.TransactionResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class JDChainResource extends Resource {

    private Boolean isInit = false;
    @JsonIgnore protected List<BlockchainKeypair> adminKey = new ArrayList<BlockchainKeypair>();
    @JsonIgnore protected HashDigest ledgerHash;

    @JsonIgnore
    protected List<BlockchainService> blockchainService = new ArrayList<BlockchainService>();

    protected Path path;

    public void init(
            List<BlockchainKeypair> adminKey,
            HashDigest ledgerHash,
            List<BlockchainService> blockchainService) {
        if (!isInit) {
            this.adminKey = adminKey;
            this.ledgerHash = ledgerHash;
            this.blockchainService = blockchainService;
            isInit = true;
        }
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
        return "JD_RESOURCE";
    }

    @Override
    public TransactionResponse call(TransactionRequest request) {
        return null;
    }

    @Override
    public TransactionResponse sendTransaction(TransactionRequest request) {
        return null;
    }

    @Override
    public void registerEventHandler(EventCallback callback) {}

    @Override
    public int getDistance() {
        return 0;
    }

    @Override
    public String getChecksum() {
        return null;
    }
}
