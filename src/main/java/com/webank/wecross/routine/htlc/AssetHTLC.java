package com.webank.wecross.routine.htlc;

import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import java.math.BigInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssetHTLC implements HTLC {
    private Logger logger = LoggerFactory.getLogger(AssetHTLC.class);

    public String lock(HTLCResource htlcResource, String h) throws Exception {
        TransactionRequest request = new TransactionRequest("lock", new String[] {h});

        TransactionContext<TransactionRequest> transactionContext =
                new TransactionContext<TransactionRequest>(
                        request,
                        htlcResource.getAccount(),
                        htlcResource.getResourceInfo(),
                        htlcResource.getResourceBlockHeaderManager());
        logger.info("lock request: {}, path: {}", request.toString(), htlcResource.getPath());
        TransactionResponse response = htlcResource.sendTransaction(transactionContext);
        if (response.getErrorCode() != 0) {
            throw new Exception(
                    "ErrorCode: "
                            + response.getErrorCode()
                            + " ErrorMessage: "
                            + response.getErrorMessage());
        }
        logger.info("lock response: {}", response.toString());
        return response.getHash();
    }

    public String unlock(
            HTLCResource selfHTLCResource, HTLCResource otherHTLCResource, String h, String s)
            throws Exception {
        // lock yourself to get transaction hash
        String transactionHash = lock(selfHTLCResource, h);
        TransactionRequest request =
                new TransactionRequest("unlock", new String[] {transactionHash, h, s});

        TransactionContext<TransactionRequest> transactionContext =
                new TransactionContext<TransactionRequest>(
                        request,
                        otherHTLCResource.getAccount(),
                        otherHTLCResource.getResourceInfo(),
                        otherHTLCResource.getResourceBlockHeaderManager());
        logger.info(
                "unlock request: {}, path: {}", request.toString(), otherHTLCResource.getPath());
        TransactionResponse response = otherHTLCResource.sendTransaction(transactionContext);
        if (response.getErrorCode() != 0) {
            throw new Exception(
                    "ErrorCode: "
                            + response.getErrorCode()
                            + " ErrorMessage: "
                            + response.getErrorMessage());
        }
        logger.info("unlock response: {}", response.toString());
        return response.getHash();
    }

    public String rollback(HTLCResource htlcResource, String h) throws Exception {
        return sendTransaction(htlcResource, "String", "rollback", h);
    }

    public String verifyLock(Resource htlcResource, String transactionHash) {
        // TODO: finish verify
        return "true";
    }

    public String verifyUnlock(Resource htlcResource, String transactionHash) {

        // TODO: finish verify
        return "true";
    }

    public String getTask(HTLCResource htlcResource) throws Exception {
        return call(htlcResource, "getTask");
    }

    public String deleteTask(HTLCResource htlcResource, String h) throws Exception {
        return sendTransaction(htlcResource, "deleteTask", h);
    }

    public String getSecret(HTLCResource htlcResource, String h) throws Exception {
        return call(htlcResource, "getSecret", h);
    }

    public BigInteger getSelfTimelock(HTLCResource htlcResource, String h) throws Exception {
        return new BigInteger(call(htlcResource, "getSelfTimelock", h));
    }

    public BigInteger getCounterpartyTimelock(HTLCResource htlcResource, String h)
            throws Exception {
        return new BigInteger(call(htlcResource, "getCounterpartyTimelock", h));
    }

    public boolean getCounterpartyLockStatus(HTLCResource htlcResource, String h) throws Exception {
        String result = call(htlcResource, "getCounterpartyLockStatus", h);
        return result.trim().equalsIgnoreCase("true");
    }

    public boolean getSelfUnlockStatus(HTLCResource htlcResource, String h) throws Exception {
        String result = call(htlcResource, "getSelfUnlockStatus", h);
        return result.trim().equalsIgnoreCase("true");
    }

    public boolean getCounterpartyUnlockStatus(HTLCResource htlcResource, String h)
            throws Exception {
        String result = call(htlcResource, "getCounterpartyUnlockStatus", h);
        return result.trim().equalsIgnoreCase("true");
    }

    public boolean getSelfRollbackStatus(HTLCResource htlcResource, String h) throws Exception {
        String result = call(htlcResource, "getSelfRollbackStatus", h);
        return result.trim().equalsIgnoreCase("true");
    }

    public boolean getCounterpartyRollbackStatus(HTLCResource htlcResource, String h)
            throws Exception {
        String result = call(htlcResource, "getCounterpartyRollbackStatus", h);
        return result.trim().equalsIgnoreCase("true");
    }

    public void setCounterpartyLockStatus(HTLCResource htlcResource, String h) throws Exception {
        sendTransaction(htlcResource, "setCounterpartyLockStatus", h);
    }

    public void setCounterpartyUnlockStatus(HTLCResource htlcResource, String h) throws Exception {
        sendTransaction(htlcResource, "setCounterpartyUnlockStatus", h);
    }

    public void setCounterpartyRollbackStatus(HTLCResource htlcResource, String h)
            throws Exception {
        sendTransaction(htlcResource, "setCounterpartyRollbackStatus", h);
    }

    private String call(HTLCResource htlcResource, String method, String... args) throws Exception {
        TransactionRequest request = new TransactionRequest(method, args);

        TransactionContext<TransactionRequest> transactionContext =
                new TransactionContext<TransactionRequest>(
                        request,
                        htlcResource.getAccount(),
                        htlcResource.getResourceInfo(),
                        htlcResource.getResourceBlockHeaderManager());
        logger.info("call request: {}, path: {}", request.toString(), htlcResource.getPath());
        TransactionResponse response = htlcResource.call(transactionContext);
        if (response.getErrorCode() != 0) {
            throw new Exception(
                    "ErrorCode: "
                            + response.getErrorCode()
                            + " ErrorMessage: "
                            + response.getErrorMessage());
        }
        logger.info("call response: {}", response.toString());
        return response.getResult()[0].trim();
    }

    private String sendTransaction(HTLCResource htlcResource, String method, String... args)
            throws Exception {
        TransactionRequest request = new TransactionRequest(method, args);

        TransactionContext<TransactionRequest> transactionContext =
                new TransactionContext<TransactionRequest>(
                        request,
                        htlcResource.getAccount(),
                        htlcResource.getResourceInfo(),
                        htlcResource.getResourceBlockHeaderManager());
        logger.info(
                "sendTransaction request: {}, path: {}",
                request.toString(),
                htlcResource.getPath());
        TransactionResponse response = htlcResource.sendTransaction(transactionContext);
        if (response.getErrorCode() != 0) {
            throw new Exception(
                    "ErrorCode: "
                            + response.getErrorCode()
                            + " ErrorMessage: "
                            + response.getErrorMessage());
        }
        logger.info("sendTransaction response: {}", response.toString());
        return response.getResult()[0].trim();
    }
}
