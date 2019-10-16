package com.webank.wecross.jdchain;

import com.jd.blockchain.ledger.TransactionTemplate;
import com.jd.blockchain.sdk.BlockchainService;
import com.webank.wecross.resource.EventCallback;
import com.webank.wecross.resource.GetDataRequest;
import com.webank.wecross.resource.GetDataResponse;
import com.webank.wecross.resource.SetDataRequest;
import com.webank.wecross.resource.SetDataResponse;
import com.webank.wecross.resource.TransactionRequest;
import com.webank.wecross.resource.TransactionResponse;
import com.webank.wecross.resource.URI;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class JdChainContractResource extends JdChainResource {
    private Boolean isInit = false;
    private String contractAddress;

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
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

        JdChainResponse response = new JdChainResponse();
        int channelCount = this.blockchainService.size();
        if (channelCount == 0) {
            response.setErrorCode(-1);
            response.setErrorMessage("has no gate way to connect");
        }
        SecureRandom random = null;
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        Integer randNum = random.nextInt(channelCount);
        for (int index = 0; index < channelCount; ++index) {
            BlockchainService blockChainService = blockchainService.get(randNum);
            TransactionTemplate txTpl = blockChainService.newTransaction(ledgerHash);
        }

        return response;
    }

    @Override
    public TransactionResponse sendTransaction(TransactionRequest request) {
        return this.call(request);
    }

    @Override
    public void registerEventHandler(EventCallback callback) {}

    @Override
    public TransactionRequest createRequest() {
        return new JdChainRequest();
    }
}
