package com.webank.wecross.resource;

import com.webank.wecross.common.WeCrossType;
import com.webank.wecross.peer.Peer;
import com.webank.wecross.restserver.request.GetDataRequest;
import com.webank.wecross.restserver.request.ProposalRequest;
import com.webank.wecross.restserver.request.SetDataRequest;
import com.webank.wecross.restserver.request.TransactionRequest;
import com.webank.wecross.restserver.response.GetDataResponse;
import com.webank.wecross.restserver.response.ProposalResponse;
import com.webank.wecross.restserver.response.SetDataResponse;
import com.webank.wecross.restserver.response.TransactionResponse;
import com.webank.wecross.utils.core.HashUtils;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// No reliable chain, just respond what you call
public class TestResource extends Resource {
    private Logger logger = LoggerFactory.getLogger(TestResource.class);

    protected Path path;
    protected String checksum;

    @Override
    public String getType() {
        return WeCrossType.RESOURCE_TYPE_TEST;
    }

    @Override
    public TransactionResponse call(TransactionRequest request) {
        TransactionResponse response = new TransactionResponse();
        response.setErrorCode(0);
        response.setErrorMessage("call test resource success");
        response.setHash("010157f4");
        response.setResult(new Object[] {request});
        return response;
    }

    @Override
    public TransactionResponse sendTransaction(TransactionRequest request) {
        TransactionResponse response = new TransactionResponse();
        response.setErrorCode(0);
        response.setErrorMessage("sendTransaction test resource success");
        response.setHash("010157f4");
        response.setResult(new Object[] {request});
        return response;
    }

    @Override
    public void registerEventHandler(EventCallback callback) {}

    @Override
    public int getDistance() {
        return 0;
    }

    @Override
    public String getChecksum() {
        try {
            if (checksum == null || checksum.isEmpty()) {
                checksum = HashUtils.sha256String(path.toString());
            }

        } catch (Exception e) {
            logger.error("Caculate checksum exception: " + e);
        }
        return checksum;
    }
}
