package com.webank.wecross.routine.htlc;

import com.webank.wecross.common.ResourceQueryStatus;
import com.webank.wecross.common.WeCrossType;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.resource.EventCallback;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssetHTLCResource extends Resource {

    private Logger logger = LoggerFactory.getLogger(AssetHTLCResource.class);

    private Resource originResource;

    public AssetHTLCResource(Resource originResource) {
        this.originResource = originResource;
    }

    @Override
    public String getType() {
        return WeCrossType.RESOURCE_TYPE_ASSET_HTLC_CONTRACT;
    }

    @Override
    public TransactionResponse call(TransactionContext<TransactionRequest> request) {
        TransactionRequest newRequest;
        try {
            newRequest = handleCallRequest(request.getData());
        } catch (WeCrossException e) {
            TransactionResponse transactionResponse = new TransactionResponse();
            transactionResponse.setErrorCode(e.getErrorCode());
            transactionResponse.setErrorMessage(e.getMessage());
            return transactionResponse;
        }
        return originResource.call(request);
    }

    @Override
    public TransactionResponse sendTransaction(TransactionContext<TransactionRequest> request) {
        TransactionRequest newRequest;
        try {
            newRequest = handleSendTransactionRequest(request.getData());
        } catch (WeCrossException e) {
            TransactionResponse transactionResponse = new TransactionResponse();
            transactionResponse.setErrorCode(e.getErrorCode());
            transactionResponse.setErrorMessage(e.getMessage());
            return transactionResponse;
        }

        return originResource.sendTransaction(request);
    }

    @Override
    public void registerEventHandler(EventCallback callback) {}

    @Override
    public String getChecksum() {
        return originResource.getChecksum();
    }

    public TransactionRequest handleSendTransactionRequest(TransactionRequest request)
            throws WeCrossException {
        if (request.getMethod().equals("unlock")) {
            String[] args = request.getArgs();
            if (args == null || args.length < 2) {
                logger.error("format of request is error in sendTransaction for unlock");
                throw new WeCrossException(
                        ResourceQueryStatus.ASSET_HTLC_REQUEST_ERROR,
                        "hash of lock transaction not found");
            }
            String transactionHash = args[0];

            AssetHTLC assetHTLC = new AssetHTLC();
            // Verify that the asset is locked
            if (!assetHTLC
                    .verifyLock(originResource, transactionHash)
                    .trim()
                    .equalsIgnoreCase("true")) {
                throw new WeCrossException(
                        ResourceQueryStatus.ASSET_HTLC_VERIFY_LOCK_ERROR,
                        "verify transaction of lock failed");
            }
            request.setArgs(Arrays.copyOfRange(args, 1, args.length - 1));
        }

        logger.info("HTLCRequest: {}", request.toString());
        return request;
    }

    public TransactionRequest handleCallRequest(TransactionRequest request)
            throws WeCrossException {
        if (request.getMethod().equals("getSecret")) {
            if (request.isFromP2P()) {
                throw new WeCrossException(
                        ResourceQueryStatus.ASSET_HTLC_NO_PERMISSION,
                        "cannot call getSecret by rpc interface");
            }
        }
        return request;
    }
}
