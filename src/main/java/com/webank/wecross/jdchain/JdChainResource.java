package com.webank.wecross.jdchain;

import com.jd.blockchain.crypto.HashDigest;
import com.jd.blockchain.ledger.BlockchainKeypair;
import com.jd.blockchain.sdk.BlockchainService;
import com.webank.wecross.resource.EventCallback;
import com.webank.wecross.resource.GetDataRequest;
import com.webank.wecross.resource.GetDataResponse;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.resource.SetDataRequest;
import com.webank.wecross.resource.SetDataResponse;
import com.webank.wecross.resource.TransactionRequest;
import com.webank.wecross.resource.TransactionResponse;
import com.webank.wecross.resource.URI;
import java.util.ArrayList;
import java.util.List;

public class JdChainResource implements Resource {

    private Boolean isInit = false;
    protected BlockchainKeypair adminKey;
    protected HashDigest ledgerHash;
    protected List<BlockchainService> blockchainService = new ArrayList<BlockchainService>();

    public void init(
            BlockchainKeypair adminKey,
            HashDigest ledgerHash,
            List<BlockchainService> blockchainService) {
        if (!isInit) {
            this.adminKey = adminKey;
            this.ledgerHash = ledgerHash;
            this.blockchainService = blockchainService;
            isInit = true;
        }
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

    @Override
    public URI getURI() {
        return null;
    }

    @Override
    public GetDataResponse getData(GetDataRequest request) {
        return null;
    }

    @Override
    public SetDataResponse setData(SetDataRequest request) {
        return null;
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
    public TransactionRequest createRequest() {
        return null;
    }
}
