package com.webank.wecross.routine.htlc;

import com.webank.wecross.routine.task.TaskManager;
import java.math.BigInteger;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTLCScheduler {
    private Logger logger = LoggerFactory.getLogger(TaskManager.class);

    private HTLC htlc;

    public HTLCScheduler(HTLC htlc) {
        this.htlc = htlc;
    }

    public void start(HTLCResourcePair htlcResourcePair, String h) throws Exception {
        boolean taskDone = false;
        HTLCResource selfHTLCResource = htlcResourcePair.getSelfHTLCResource();
        HTLCResource counterpartyHTLCResource = htlcResourcePair.getCounterpartyHTLCResource();

        boolean selfRolledback = checkSelfRollback(selfHTLCResource, h);
        boolean counterpartyRolledback = checkCounterpartyRollback(selfHTLCResource, h);
        // the initiator and participant can still do their jobs even though the participant seems
        // to be rolled back
        if (selfRolledback && counterpartyRolledback) {
            taskDone = true;
        } else {
            String s = getSecret(selfHTLCResource, h);
            if (!s.trim().equalsIgnoreCase("null")) {
                boolean otherUnlocked = htlc.getCounterpartyUnlockStatus(selfHTLCResource, h);
                boolean selfUnlocked = htlc.getSelfUnlockStatus(selfHTLCResource, h);
                if (otherUnlocked) {
                    if (selfUnlocked) {
                        taskDone = true;
                    }
                    // else: do nothing but wait to roll back
                } else {
                    boolean counterpartyLocked =
                            htlc.getCounterpartyLockStatus(selfHTLCResource, h);
                    if (!counterpartyLocked) {
                        String lockHash = htlc.lock(counterpartyHTLCResource, h);
                        String verifyResult =
                                htlc.verifyLock(selfHTLCResource.getOriginResource(), lockHash);
                        if (!verifyResult.trim().equalsIgnoreCase("true")) {
                            if (counterpartyRolledback) {
                                // the initiator executes roll back
                                htlc.rollback(selfHTLCResource, h);
                                taskDone = true;
                            } else {
                                logger.error(
                                        "task: {}, verifying lock failed: {}", h, verifyResult);
                                return;
                            }
                        } else {
                            htlc.setCounterpartyLockStatus(selfHTLCResource, h);
                            logger.info("lock succeeded: {}", h);
                        }
                    }

                    if (!taskDone) {
                        String unlockHash =
                                htlc.unlock(selfHTLCResource, counterpartyHTLCResource, h, s);
                        String verifyResult =
                                htlc.verifyUnlock(selfHTLCResource.getOriginResource(), unlockHash);
                        if (!verifyResult.trim().equalsIgnoreCase("true")) {
                            if (counterpartyRolledback) {
                                htlc.rollback(selfHTLCResource, h);
                                taskDone = true;
                            } else {
                                logger.error(
                                        "task: {}, verifying unlock failed: {}", h, verifyResult);
                                return;
                            }
                        } else {
                            if (selfUnlocked) {
                                taskDone = true;
                            }
                            htlc.setCounterpartyUnlockStatus(selfHTLCResource, h);
                            logger.info("unlock succeeded: {}", h);
                        }
                    }
                }
            }
        }

        if (taskDone) {
            htlc.deleteTask(selfHTLCResource, h);
            logger.info("current task completed: {}", h);
        }
    }

    public String getSecret(HTLCResource htlcResource, String h) {
        String result = "null";

        Callable<String> task =
                new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        String s = "null";
                        while (s.trim().equalsIgnoreCase("null")) {
                            s = htlc.getSecret(htlcResource, h);
                            if (s.equalsIgnoreCase("null")) {
                                Thread.sleep(1000);
                            }
                        }
                        logger.info("assetHtlc task h: {}, s: {}", h, s);
                        return s;
                    }
                };

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<String> future = executorService.submit(task);
        try {
            // set timeout of getSecret
            result = future.get(4000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            logger.warn("timeout in getSecret: {}", h);
        } catch (ExecutionException | InterruptedException e) {
            logger.warn("failed to getSecret: {}", h);
        } finally {
            executorService.shutdown();
        }
        return result;
    }

    public boolean checkSelfRollback(HTLCResource htlcResource, String h) throws Exception {
        if (htlc.getSelfRollbackStatus(htlcResource, h)) {
            return true;
        }

        BigInteger selfTimelock = htlc.getSelfTimelock(htlcResource, h);
        BigInteger now = BigInteger.valueOf(System.currentTimeMillis());
        logger.info("task: {}, selfTimelock: {}, now: {}", h, selfTimelock, now);
        if (now.compareTo(selfTimelock) >= 0) {
            htlc.rollback(htlcResource, h);
            return true;
        }
        return false;
    }

    public boolean checkCounterpartyRollback(HTLCResource htlcResource, String h) throws Exception {
        if (htlc.getCounterpartyRollbackStatus(htlcResource, h)) {
            return true;
        }

        BigInteger counterpartyTimelock = htlc.getCounterpartyTimelock(htlcResource, h);
        BigInteger now = BigInteger.valueOf(System.currentTimeMillis());
        logger.info("task: {}, counterpartyTimelock: {}, now: {}", h, counterpartyTimelock, now);
        if (now.compareTo(counterpartyTimelock) >= 0) {
            htlc.setCounterpartyRollbackStatus(htlcResource, h);
            return true;
        }
        return false;
    }

    public String getTask(HTLCResource htlcResource) throws Exception {
        return htlc.getTask(htlcResource);
    }
}
