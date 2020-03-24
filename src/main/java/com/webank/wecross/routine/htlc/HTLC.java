package com.webank.wecross.routine.htlc;

import com.webank.wecross.resource.Resource;
import java.math.BigInteger;

public interface HTLC {
    String lock(HTLCResource htlcResource, String h) throws Exception;

    String unlock(
            HTLCResource selfHTLCResource,
            HTLCResource counterpartyHTLCResource,
            String h,
            String s)
            throws Exception;

    String rollback(HTLCResource htlcResource, String h) throws Exception;

    String verifyLock(Resource htlcResource, String transactionHash);

    String verifyUnlock(Resource htlcResource, String transactionHash);

    String getTask(HTLCResource htlcResource) throws Exception;

    String deleteTask(HTLCResource htlcResource, String h) throws Exception;

    String getSecret(HTLCResource htlcResource, String h) throws Exception;

    BigInteger getSelfTimelock(HTLCResource htlcResource, String h) throws Exception;

    BigInteger getCounterpartyTimelock(HTLCResource htlcResource, String h) throws Exception;

    boolean getCounterpartyLockStatus(HTLCResource htlcResource, String h) throws Exception;

    boolean getSelfUnlockStatus(HTLCResource htlcResource, String h) throws Exception;

    boolean getCounterpartyUnlockStatus(HTLCResource htlcResource, String h) throws Exception;

    boolean getSelfRollbackStatus(HTLCResource htlcResource, String h) throws Exception;

    boolean getCounterpartyRollbackStatus(HTLCResource htlcResource, String h) throws Exception;

    void setCounterpartyLockStatus(HTLCResource htlcResource, String h) throws Exception;

    void setCounterpartyUnlockStatus(HTLCResource htlcResource, String h) throws Exception;

    void setCounterpartyRollbackStatus(HTLCResource htlcResource, String h) throws Exception;
}
