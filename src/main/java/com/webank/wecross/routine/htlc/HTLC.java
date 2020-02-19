package com.webank.wecross.routine.htlc;

import com.webank.wecross.resource.Resource;
import java.math.BigInteger;

public interface HTLC {
    String lock(Resource htlcResource, String h) throws Exception;

    String unlock(Resource selfHTLCResource, Resource counterpartyHTLCResource, String h, String s)
            throws Exception;

    String rollback(Resource htlcResource, String h) throws Exception;

    String verifyLock(Resource htlcResource, String transactionHash);

    String verifyUnlock(Resource htlcResource, String transactionHash);

    String getCounterpartyHTLCIpath(Resource htlcResource) throws Exception;

    String getTask(Resource htlcResource) throws Exception;

    String deleteTask(Resource htlcResource, String h) throws Exception;

    String getSecret(Resource htlcResource, String h) throws Exception;

    BigInteger getSelfTimelock(Resource htlcResource, String h) throws Exception;

    BigInteger getCounterpartyTimelock(Resource htlcResource, String h) throws Exception;

    boolean getSelfLockStatus(Resource htlcResource, String h) throws Exception;

    boolean getCounterpartyLockStatus(Resource htlcResource, String h) throws Exception;

    boolean getSelfUnlockStatus(Resource htlcResource, String h) throws Exception;

    boolean getCounterpartyUnlockStatus(Resource htlcResource, String h) throws Exception;

    boolean getSelfRollbackStatus(Resource htlcResource, String h) throws Exception;

    boolean getCounterpartyRollbackStatus(Resource htlcResource, String h) throws Exception;

    void setCounterpartyLockStatus(Resource htlcResource, String h) throws Exception;

    void setCounterpartyUnlockStatus(Resource htlcResource, String h) throws Exception;

    void setCounterpartyRollbackStatus(Resource htlcResource, String h) throws Exception;
}
