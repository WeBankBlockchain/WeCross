package com.webank.wecross.jdchain;

import com.jd.blockchain.ledger.TransactionTemplate;
import com.jd.blockchain.sdk.BlockchainService;
import com.webank.wecross.resource.EventCallback;
import com.webank.wecross.resource.GetDataRequest;
import com.webank.wecross.resource.GetDataResponse;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.SetDataRequest;
import com.webank.wecross.resource.SetDataResponse;
import com.webank.wecross.resource.TransactionRequest;
import com.webank.wecross.resource.TransactionResponse;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDChainContractResource extends JDChainResource {

    private Logger logger = LoggerFactory.getLogger(JDChainContractResource.class);
    private Boolean isInit = false;
    private String contractAddress;

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    @Override
    public Path getPath() {
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
            logger.error("rand Algorithm:{}", e);
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
