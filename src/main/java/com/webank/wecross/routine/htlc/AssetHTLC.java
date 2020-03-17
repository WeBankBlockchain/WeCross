package com.webank.wecross.routine.htlc;

import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import java.math.BigInteger;

public class AssetHTLC implements HTLC {

    public String lock(HTLCResource htlcResource, String h) throws Exception {
        TransactionRequest request = new TransactionRequest("lock", new String[] {h});

        TransactionContext<TransactionRequest> transactionContext =
                new TransactionContext<TransactionRequest>(
                        request, htlcResource.getAccount(), htlcResource.getResourceInfo());

        TransactionResponse response = htlcResource.sendTransaction(transactionContext);
        if (response.getErrorCode() != 0) {
            throw new Exception(
                    "ErrorCode: "
                            + response.getErrorCode()
                            + " ErrorMessage: "
                            + response.getErrorMessage());
        }
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
                        otherHTLCResource.getResourceInfo());

        TransactionResponse response = otherHTLCResource.sendTransaction(transactionContext);
        if (response.getErrorCode() != 0) {
            throw new Exception(
                    "ErrorCode: "
                            + response.getErrorCode()
                            + " ErrorMessage: "
                            + response.getErrorMessage());
        }
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

    public String getCounterpartyHTLCPath(HTLCResource htlcResource) throws Exception {
        return call(htlcResource, "String", "getCounterpartyHTLCPath");
    }

    public String getTask(HTLCResource htlcResource) throws Exception {
        return call(htlcResource, "String", "getTask");
    }

    public String deleteTask(HTLCResource htlcResource, String h) throws Exception {
        return sendTransaction(htlcResource, "String", "deleteTask", h);
    }

    public String getSecret(HTLCResource htlcResource, String h) throws Exception {
        TransactionRequest request = new TransactionRequest("getSecret", new String[] {h}, false);

        // TODO: fill TransactionContext
        TransactionContext<TransactionRequest> transactionContext =
                new TransactionContext<TransactionRequest>(
                        request, htlcResource.getAccount(), htlcResource.getResourceInfo());

        TransactionResponse response = htlcResource.call(transactionContext); // TODO: fix it
        if (response.getErrorCode() != 0) {
            throw new Exception(
                    "ErrorCode: "
                            + response.getErrorCode()
                            + " ErrorMessage: "
                            + response.getErrorMessage());
        }
        return response.getResult()[0];
    }

    public BigInteger getSelfTimelock(HTLCResource htlcResource, String h) throws Exception {
        String result = call(htlcResource, "String", "getSelfTimelock", h);
        return new BigInteger(result);
    }

    public BigInteger getCounterpartyTimelock(HTLCResource htlcResource, String h)
            throws Exception {
        String result = call(htlcResource, "String", "getCounterpartyTimelock", h);
        return new BigInteger(result);
    }

    public boolean getCounterpartyLockStatus(HTLCResource htlcResource, String h) throws Exception {
        String result = call(htlcResource, "String", "getCounterpartyLockStatus", h);
        return result.trim().equalsIgnoreCase("true");
    }

    public boolean getSelfUnlockStatus(HTLCResource htlcResource, String h) throws Exception {
        String result = call(htlcResource, "String", "getSelfUnlockStatus", h);
        return result.trim().equalsIgnoreCase("true");
    }

    public boolean getCounterpartyUnlockStatus(HTLCResource htlcResource, String h)
            throws Exception {
        String result = call(htlcResource, "String", "getCounterpartyUnlockStatus", h);
        return result.trim().equalsIgnoreCase("true");
    }

    public boolean getSelfRollbackStatus(HTLCResource htlcResource, String h) throws Exception {
        String result = call(htlcResource, "String", "getSelfRollbackStatus", h);
        return result.trim().equalsIgnoreCase("true");
    }

    public boolean getCounterpartyRollbackStatus(HTLCResource htlcResource, String h)
            throws Exception {
        String result = call(htlcResource, "String", "getCounterpartyRollbackStatus", h);
        return result.trim().equalsIgnoreCase("true");
    }

    public void setCounterpartyLockStatus(HTLCResource htlcResource, String h) throws Exception {
        sendTransaction(htlcResource, "", "setCounterpartyLockStatus", h);
    }

    public void setCounterpartyUnlockStatus(HTLCResource htlcResource, String h) throws Exception {
        sendTransaction(htlcResource, "", "setCounterpartyUnlockStatus", h);
    }

    public void setCounterpartyRollbackStatus(HTLCResource htlcResource, String h)
            throws Exception {
        sendTransaction(htlcResource, "", "setCounterpartyRollbackStatus", h);
    }

    private static String call(HTLCResource htlcResource, String method, String... args)
            throws Exception {
        TransactionRequest request = new TransactionRequest(method, args);

        TransactionContext<TransactionRequest> transactionContext =
                new TransactionContext<TransactionRequest>(
                        request, htlcResource.getAccount(), htlcResource.getResourceInfo());
        TransactionResponse response = htlcResource.call(transactionContext);
        if (response.getErrorCode() != 0) {
            throw new Exception(
                    "ErrorCode: "
                            + response.getErrorCode()
                            + " ErrorMessage: "
                            + response.getErrorMessage());
        }
        return response.getResult()[0];
    }

    private static String sendTransaction(HTLCResource htlcResource, String method, String... args)
            throws Exception {
        TransactionRequest request = new TransactionRequest(method, args);

        TransactionContext<TransactionRequest> transactionContext =
                new TransactionContext<TransactionRequest>(
                        request, htlcResource.getAccount(), htlcResource.getResourceInfo());

        TransactionResponse response = htlcResource.sendTransaction(transactionContext);
        if (response.getErrorCode() != 0) {
            throw new Exception(
                    "ErrorCode: "
                            + response.getErrorCode()
                            + " ErrorMessage: "
                            + response.getErrorMessage());
        }
        return response.getResult()[0];
    }
}
