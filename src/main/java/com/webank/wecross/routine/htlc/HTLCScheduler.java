package com.webank.wecross.routine.htlc;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.exception.WeCrossException.ErrorCode;
import com.webank.wecross.routine.RoutineDefault;
import com.webank.wecross.stub.TransactionResponse;
import com.webank.wecross.stub.VerifiedTransaction;
import java.math.BigInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTLCScheduler {
    private Logger logger = LoggerFactory.getLogger(HTLCScheduler.class);

    private HTLC htlc;

    public HTLCScheduler(HTLC htlc) {
        this.htlc = htlc;
    }

    public void start(HTLCResourcePair htlcResourcePair, String h) throws WeCrossException {
        boolean taskDone = false;
        HTLCResource selfResource = htlcResourcePair.getSelfHTLCResource();
        boolean selfRolledback = checkSelfRollback(selfResource, h);
        boolean counterpartyRolledback = checkCounterpartyRollback(selfResource, h);

        // the initiator and participant can still do their jobs even though the participant seems
        // to be rolled back
        if (selfRolledback && counterpartyRolledback) {
            taskDone = true;
        } else {

            HTLCResource counterpartyResource = htlcResourcePair.getCounterpartyHTLCResource();
            if (counterpartyResource.getSelfResource() == null) {
                throw new WeCrossException(
                        ErrorCode.HTLC_ERROR,
                        "error in scheduler",
                        HTLCErrorCode.NO_COUNTERPARTY_RESOURCE,
                        "counterparty resource: "
                                + counterpartyResource.getSelfPath().toString()
                                + " not found");
            }

            String s = getSecret(selfResource, h);
            if (!s.equalsIgnoreCase(RoutineDefault.NULL_FLAG)) {
                logger.info(
                        "get secret successfully, s: {}, h: {}, path: {}",
                        s,
                        h,
                        selfResource.getSelfPath().toString());

                // check that the contract data in two chains is consistent
                if (!checkContractInfo(htlcResourcePair, h)) {
                    // delete invalid task
                    htlc.deleteTask(selfResource, h);
                    logger.info(
                            "inconsistent contract data, delete current task: {}, path: {}",
                            h,
                            selfResource.getSelfPath().toString());
                    return;
                }

                boolean otherUnlocked = htlc.getCounterpartyUnlockStatus(selfResource, h);
                boolean selfUnlocked = htlc.getSelfUnlockStatus(selfResource, h);
                if (!otherUnlocked) {
                    // lock self
                    String[] lockTxInfo = handleSelfLock(selfResource, h);

                    String lockTxHash = lockTxInfo[0];
                    long lockTxBlockNum = Long.parseLong(lockTxInfo[1]);

                    // lock counterparty
                    handleCounterpartyLock(selfResource, counterpartyResource, h);

                    counterpartyResource.setCounterpartyAddress(
                            htlc.getCounterpartyHtlc(
                                    counterpartyResource.getSelfResource(),
                                    counterpartyResource.getAccount()));

                    // unlock counterparty
                    htlc.unlockWithVerify(
                            counterpartyResource,
                            lockTxHash,
                            lockTxBlockNum,
                            selfResource.getCounterpartyAddress(),
                            h,
                            s);
                    htlc.setCounterpartyUnlockStatus(selfResource, h);
                    logger.info("unlock succeeded: {}, path: {}", h, selfResource.getSelfPath());
                }

                // else: do nothing but wait to roll back
                if (selfUnlocked) {
                    taskDone = true;
                }
            }
        }

        if (taskDone) {
            htlc.deleteTask(selfResource, h);
            logger.info("current htlc task completed: {},path: {}", h, selfResource.getSelfPath());
        }
    }

    public boolean checkContractInfo(HTLCResourcePair htlcResourcePair, String h)
            throws WeCrossException {
        HTLCResource selfResource = htlcResourcePair.getSelfHTLCResource();
        HTLCResource counterpartyResource = htlcResourcePair.getCounterpartyHTLCResource();
        String address = selfResource.getCounterpartyAddress();

        String[] info0 = htlc.getNewContractTxInfo(selfResource, h);
        VerifiedTransaction verifiedTransaction0 =
                selfResource
                        .getDriver()
                        .getVerifiedTransaction(
                                info0[0],
                                Long.parseLong(info0[1]),
                                selfResource.getResourceBlockHeaderManager(),
                                selfResource.chooseConnection());

        logger.debug(
                "verifiedTransaction for self transfer contract: {}",
                verifiedTransaction0.toString());

        String[] args = verifiedTransaction0.getTransactionRequest().getArgs();
        if (args[1].equalsIgnoreCase(RoutineDefault.TRUE_FLAG)) {
            args[1] = RoutineDefault.FALSE_FLAG;
        } else {
            args[1] = RoutineDefault.TRUE_FLAG;
        }

        String[] output = verifiedTransaction0.getTransactionResponse().getResult();

        String[] info1 = htlc.getNewContractTxInfo(counterpartyResource, h);
        VerifyData verifyData =
                new VerifyData(
                        Long.parseLong(info1[1]), info1[0], address, "newContract", args, output);

        VerifiedTransaction verifiedTransaction1 =
                counterpartyResource
                        .getDriver()
                        .getVerifiedTransaction(
                                info1[0],
                                Long.parseLong(info1[1]),
                                counterpartyResource.getResourceBlockHeaderManager(),
                                counterpartyResource.chooseConnection());

        logger.debug(
                "verifiedTransaction for counterparty transfer contract: {}",
                verifiedTransaction1.toString());

        return verifyData.verify(verifiedTransaction1);
    }

    public boolean checkSelfRollback(HTLCResource htlcResource, String h) throws WeCrossException {
        if (htlc.getSelfRollbackStatus(htlcResource, h)) {
            return true;
        }

        BigInteger selfTimelock = htlc.getSelfTimelock(htlcResource, h);
        BigInteger now = BigInteger.valueOf(System.currentTimeMillis() / 1000);
        logger.info(
                "task: {}, selfTimelock: {}, now: {}, path: {}",
                h,
                selfTimelock,
                now,
                htlcResource.getSelfPath().toString());
        if (now.compareTo(selfTimelock) >= 0) {
            String result = htlc.rollback(htlcResource, h);
            {
                if (!result.equalsIgnoreCase(RoutineDefault.SUCCESS_FLAG)) {
                    if (result.equalsIgnoreCase(RoutineDefault.NOT_YET)) {
                        return false;
                    }
                    logger.info(
                            "no need to rollback, reason: {}, task: {}, path: {}",
                            h,
                            result,
                            htlcResource.getSelfPath().toString());
                }
            }

            return true;
        }
        return false;
    }

    public boolean checkCounterpartyRollback(HTLCResource htlcResource, String h)
            throws WeCrossException {
        if (htlc.getCounterpartyRollbackStatus(htlcResource, h)) {
            return true;
        }

        BigInteger counterpartyTimelock = htlc.getCounterpartyTimelock(htlcResource, h);
        BigInteger now = BigInteger.valueOf(System.currentTimeMillis() / 1000);
        logger.info(
                "task: {}, counterpartyTimelock: {}, now: {}, path: {}",
                h,
                counterpartyTimelock,
                now,
                htlcResource.getSelfPath());
        if (now.compareTo(counterpartyTimelock) >= 0) {
            htlc.setCounterpartyRollbackStatus(htlcResource, h);
            return true;
        }
        return false;
    }

    public String getSecret(HTLCResource htlcResource, String h) throws WeCrossException {
        String result = RoutineDefault.NULL_FLAG;

        try {
            int round = 0;
            while (result.equalsIgnoreCase(RoutineDefault.NULL_FLAG) && round++ < 3) {
                result = htlc.getSecret(htlcResource, h);
                if (result.equalsIgnoreCase(RoutineDefault.NULL_FLAG)) {
                    Thread.sleep(500);
                }
            }
        } catch (Exception e) {
            logger.warn("failed to getSecret, h: {}, path: {}", h, htlcResource.getSelfPath());
        }
        return result;
    }

    public String[] handleSelfLock(HTLCResource htlcResource, String h) throws WeCrossException {
        if (htlc.getSelfLockStatus(htlcResource, h)) {
            // if self locked, query tx hash and block number
            return htlc.getLockTxInfo(htlcResource, h);
        } else {
            TransactionResponse lockResponse = htlc.lock(htlcResource, h);
            String result = lockResponse.getResult()[0].trim();
            if (!result.equalsIgnoreCase(RoutineDefault.SUCCESS_FLAG)) {
                throw new WeCrossException(
                        ErrorCode.HTLC_ERROR,
                        "error in handleSelfLock",
                        HTLCErrorCode.LOCK_ERROR,
                        "failed to lock self: " + result);
            }
            logger.info("lock self successfully: {}, path: {}", h, htlcResource.getSelfPath());
            return new String[] {
                lockResponse.getHash(), String.valueOf(lockResponse.getBlockNumber())
            };
        }
    }

    public void handleCounterpartyLock(
            HTLCResource selfResource, HTLCResource counterpartyResource, String h)
            throws WeCrossException {
        boolean counterpartyLocked = htlc.getCounterpartyLockStatus(selfResource, h);
        if (!counterpartyLocked) {
            htlc.lockWithVerify(counterpartyResource, selfResource.getCounterpartyAddress(), h);
            htlc.setCounterpartyLockStatus(selfResource, h);
            logger.info(
                    "lock counterparty successfully: {}, path: {}", h, selfResource.getSelfPath());
        }
    }

    public String getTask(HTLCResource htlcResource) throws WeCrossException {
        return htlc.getTask(htlcResource);
    }

    public HTLC getHtlc() {
        return htlc;
    }

    public void setHtlc(HTLC htlc) {
        this.htlc = htlc;
    }
}
