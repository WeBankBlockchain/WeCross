package com.webank.wecross.routine.htlc;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.TransactionResponse;
import java.math.BigInteger;

public interface HTLC {
    TransactionResponse lock(HTLCResource htlcResource, String h) throws WeCrossException;

    void lockWithVerify(HTLCResource htlcResource, String address, String h)
            throws WeCrossException;

    TransactionResponse unlock(
            HTLCResource htlcResource, String txHash, long blockNumber, String h, String s)
            throws WeCrossException;

    void unlockWithVerify(
            HTLCResource htlcResource,
            String txHash,
            long blockNumber,
            String address,
            String h,
            String s)
            throws WeCrossException;

    String rollback(HTLCResource htlcResource, String h) throws WeCrossException;

    boolean verify(Resource counterpartyResource, VerifyData verifyData);

    String getCounterpartyHtlc(Resource resource, Account account) throws WeCrossException;

    String getTask(HTLCResource htlcResource) throws WeCrossException;

    String deleteTask(HTLCResource htlcResource, String h) throws WeCrossException;

    String getSecret(HTLCResource htlcResource, String h) throws WeCrossException;

    String[] getNewContractTxInfo(HTLCResource htlcResource, String h) throws WeCrossException;

    String[] getLockTxInfo(HTLCResource htlcResource, String h) throws WeCrossException;

    void setLockTxInfo(HTLCResource htlcResource, String h, String txHash, long blockNumber)
            throws WeCrossException;

    BigInteger getSelfTimelock(HTLCResource htlcResource, String h) throws WeCrossException;

    BigInteger getCounterpartyTimelock(HTLCResource htlcResource, String h) throws WeCrossException;

    boolean getSelfLockStatus(HTLCResource htlcResource, String h) throws WeCrossException;

    boolean getCounterpartyLockStatus(HTLCResource htlcResource, String h) throws WeCrossException;

    boolean getSelfUnlockStatus(HTLCResource htlcResource, String h) throws WeCrossException;

    boolean getCounterpartyUnlockStatus(HTLCResource htlcResource, String h)
            throws WeCrossException;

    boolean getSelfRollbackStatus(HTLCResource htlcResource, String h) throws WeCrossException;

    boolean getCounterpartyRollbackStatus(HTLCResource htlcResource, String h)
            throws WeCrossException;

    void setCounterpartyLockStatus(HTLCResource htlcResource, String h) throws WeCrossException;

    void setCounterpartyUnlockStatus(HTLCResource htlcResource, String h) throws WeCrossException;

    void setCounterpartyRollbackStatus(HTLCResource htlcResource, String h) throws WeCrossException;
}
