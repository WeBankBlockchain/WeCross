package com.webank.wecross.stub.jdchain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jd.blockchain.ledger.TransactionTemplate;
import com.jd.blockchain.sdk.BlockchainService;
import com.webank.wecross.resource.EventCallback;
import com.webank.wecross.resource.request.GetDataRequest;
import com.webank.wecross.resource.request.SetDataRequest;
import com.webank.wecross.resource.request.TransactionRequest;
import com.webank.wecross.resource.response.GetDataResponse;
import com.webank.wecross.resource.response.SetDataResponse;
import com.webank.wecross.resource.response.TransactionResponse;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDChainContractResource extends JDChainResource {

    private Logger logger = LoggerFactory.getLogger(JDChainContractResource.class);
    private Boolean isInit = false;
    @JsonIgnore private String contractAddress;

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    @Override
    public String getType() {
        return "JD_CONTRACT";
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

        JDChainResponse response = new JDChainResponse();
        int channelCount = this.blockchainService.size();
        if (channelCount == 0) {
            response.setErrorCode(-1);
            response.setErrorMessage("has no gate way to connect");
        }
        SecureRandom random = null;
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            logger.error("rand Algorithm:{}", e.getMessage());
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
        return new JDChainRequest();
    }
}
