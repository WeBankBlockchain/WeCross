package com.webank.wecross.routine.htlc;

import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import java.math.BigInteger;

public class AssetHTLC implements HTLC {

    public String lock(Resource htlcResource, String h) throws Exception {
        TransactionRequest request = new TransactionRequest("lock", new String[] {h});

        // TODO: fill TransactionContext
        TransactionContext<TransactionRequest> transactionContext =
                new TransactionContext<TransactionRequest>(request, null, null);

        TransactionResponse response =
                htlcResource.sendTransaction(transactionContext); // TODO: fix it
        if (response.getErrorCode() != 0) {
            throw new Exception(
                    "ErrorCode: "
                            + response.getErrorCode()
                            + " ErrorMessage: "
                            + response.getErrorMessage());
        }
        return response.getHash();
    }

    public String unlock(Resource selfHTLCResource, Resource otherHTLCResource, String h, String s)
            throws Exception {
        // lock yourself to get transaction hash
        String transactionHash = lock(selfHTLCResource, h);
        TransactionRequest request =
                new TransactionRequest("unlock", new String[] {transactionHash, h, s});

        // TODO: fill TransactionContext
        TransactionContext<TransactionRequest> transactionContext =
                new TransactionContext<TransactionRequest>(request, null, null);

        TransactionResponse response =
                otherHTLCResource.sendTransaction(transactionContext); // TODO: fix it
        if (response.getErrorCode() != 0) {
            throw new Exception(
                    "ErrorCode: "
                            + response.getErrorCode()
                            + " ErrorMessage: "
                            + response.getErrorMessage());
        }
        return response.getHash();
    }

    public String rollback(Resource htlcResource, String h) throws Exception {
        return (String) sendTransaction(htlcResource, "String", "rollback", h);
    }

    public String verifyLock(Resource htlcResource, String transactionHash) {
        return "true";
    }

    public String verifyUnlock(Resource htlcResource, String transactionHash) {
        return "true";
    }

    public String getCounterpartyHTLCIpath(Resource htlcResource) throws Exception {
        return (String) call(htlcResource, "String", "getCounterpartyHTLCIpath");
    }

    public String getTask(Resource htlcResource) throws Exception {
        return (String) call(htlcResource, "String", "getTask");
    }

    public String deleteTask(Resource htlcResource, String h) throws Exception {
        return (String) sendTransaction(htlcResource, "String", "deleteTask", h);
    }

    public String getSecret(Resource htlcResource, String h) throws Exception {
        TransactionRequest request = new TransactionRequest("getSecret", new String[] {h}, false);

        // TODO: fill TransactionContext
        TransactionContext<TransactionRequest> transactionContext =
                new TransactionContext<TransactionRequest>(request, null, null);

        TransactionResponse response = htlcResource.call(transactionContext); // TODO: fix it
        if (response.getErrorCode() != 0) {
            throw new Exception(
                    "ErrorCode: "
                            + response.getErrorCode()
                            + " ErrorMessage: "
                            + response.getErrorMessage());
        }
        return (String) response.getResult()[0];
    }

    public BigInteger getSelfTimelock(Resource htlcResource, String h) throws Exception {
        String result = (String) call(htlcResource, "String", "getSelfTimelock", h);
        return new BigInteger(result);
    }

    public BigInteger getCounterpartyTimelock(Resource htlcResource, String h) throws Exception {
        String result = (String) call(htlcResource, "String", "getCounterpartyTimelock", h);
        return new BigInteger(result);
    }

    public boolean getCounterpartyLockStatus(Resource htlcResource, String h) throws Exception {
        String result = (String) call(htlcResource, "String", "getCounterpartyLockStatus", h);
        if (result.trim().equalsIgnoreCase("true")) {
            return true;
        }
        return false;
    }

    public boolean getSelfUnlockStatus(Resource htlcResource, String h) throws Exception {
        String result = (String) call(htlcResource, "String", "getSelfUnlockStatus", h);
        if (result.trim().equalsIgnoreCase("true")) {
            return true;
        }
        return false;
    }

    public boolean getCounterpartyUnlockStatus(Resource htlcResource, String h) throws Exception {
        String result = (String) call(htlcResource, "String", "getCounterpartyUnlockStatus", h);
        if (result.trim().equalsIgnoreCase("true")) {
            return true;
        }
        return false;
    }

    public boolean getSelfRollbackStatus(Resource htlcResource, String h) throws Exception {
        String result = (String) call(htlcResource, "String", "getSelfRollbackStatus", h);
        if (result.trim().equalsIgnoreCase("true")) {
            return true;
        }
        return false;
    }

    public boolean getCounterpartyRollbackStatus(Resource htlcResource, String h) throws Exception {
        String result = (String) call(htlcResource, "String", "getCounterpartyRollbackStatus", h);
        if (result.trim().equalsIgnoreCase("true")) {
            return true;
        }
        return false;
    }

    public void setCounterpartyLockStatus(Resource htlcResource, String h) throws Exception {
        sendTransaction(htlcResource, "", "setCounterpartyLockStatus", h);
    }

    public void setCounterpartyUnlockStatus(Resource htlcResource, String h) throws Exception {
        sendTransaction(htlcResource, "", "setCounterpartyUnlockStatus", h);
    }

    public void setCounterpartyRollbackStatus(Resource htlcResource, String h) throws Exception {
        sendTransaction(htlcResource, "", "setCounterpartyRollbackStatus", h);
    }

    private static Object call(Resource htlcResource, String retType, String method, String... args)
            throws Exception {
        TransactionRequest request = new TransactionRequest(method, args);

        // TODO: fill TransactionContext
        TransactionContext<TransactionRequest> transactionContext =
                new TransactionContext<TransactionRequest>(request, null, null);
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

    private static Object sendTransaction(
            Resource htlcResource, String retType, String method, String... args) throws Exception {
        TransactionRequest request = new TransactionRequest(method, args);

        // TODO: fill TransactionContext
        TransactionContext<TransactionRequest> transactionContext =
                new TransactionContext<TransactionRequest>(request, null, null);

        TransactionResponse response =
                htlcResource.sendTransaction(transactionContext); // TODO: fix it
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
