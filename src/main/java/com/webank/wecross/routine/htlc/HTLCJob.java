package com.webank.wecross.routine.htlc;

import com.webank.wecross.interchain.InterchainDefault;
import com.webank.wecross.routine.RoutineDefault;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.TransactionException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisallowConcurrentExecution
public class HTLCJob implements Job {
    private Logger logger = LoggerFactory.getLogger(HTLCJob.class);

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        HTLCResourcePair htlcResourcePair =
                (HTLCResourcePair) dataMap.get(RoutineDefault.HTLC_JOB_DATA_KEY);
        handleProposals(htlcResourcePair);
    }

    private void handleProposals(HTLCResourcePair htlcResourcePair) {
        // get unfinished htlc proposal
        String[] proposalIds = getProposalIDs(htlcResourcePair);
        if (proposalIds.length == 0) {
            return;
        }

        Semaphore semaphore = new Semaphore(proposalIds.length, true);
        try {
            semaphore.acquire(proposalIds.length);
        } catch (InterruptedException e) {
            logger.warn("Interrupted,", e);
            Thread.currentThread().interrupt();
        }

        for (String proposalId : proposalIds) {
            if (!RoutineDefault.NULL_FLAG.equals(proposalId)) {
                Path path = htlcResourcePair.getSelfHTLCResource().getSelfPath();
                if (logger.isDebugEnabled()) {
                    logger.debug("Start handling htlc proposal: {}, path: {}", proposalId, path);
                }
                HTLCScheduler htlcScheduler = new HTLCScheduler(htlcResourcePair.getHtlc());
                htlcScheduler.start(
                        htlcResourcePair,
                        proposalId,
                        (exception, state) -> {
                            if (exception != null) {
                                if (exception.getInternalErrorCode()
                                        == HTLCErrorCode.GET_PROPOSAL_TX_INFO_ERROR) {
                                    // This is a common situation because it's impossible for both
                                    // parties to create proposals at the same time
                                    if (logger.isDebugEnabled()) {
                                        logger.debug(
                                                "Failed to handle current proposal: {}, path: {}, errorMessage: {}",
                                                proposalId,
                                                path,
                                                exception.getInternalMessage());
                                    }
                                } else if (exception.getInternalErrorCode()
                                        == HTLCErrorCode.NO_COUNTERPARTY_RESOURCE) {
                                    // just started
                                    if (logger.isDebugEnabled()) {
                                        logger.debug(
                                                "Failed to handle current proposal: {}, path: {}, errorMessage: {}",
                                                proposalId,
                                                path,
                                                exception.getInternalMessage());
                                    }
                                } else {
                                    logger.error(
                                            "Failed to handle current proposal: {}, path: {}, errorMessage: {}, internalMessage: {}",
                                            proposalId,
                                            path,
                                            exception.getLocalizedMessage(),
                                            exception.getInternalMessage());
                                }
                            }
                            semaphore.release();
                        });
            } else {
                semaphore.release();
            }
        }

        try {
            // wait until job is finished
            semaphore.acquire(proposalIds.length);
        } catch (InterruptedException e) {
            logger.warn("Interrupted,", e);
            Thread.currentThread().interrupt();
        }
    }

    private String[] getProposalIDs(HTLCResourcePair htlcResourcePair) {
        HTLC htlc = htlcResourcePair.getHtlc();
        HTLCResource htlcResource = htlcResourcePair.getSelfHTLCResource();

        CompletableFuture<String> future = new CompletableFuture<>();
        htlc.getProposalIDs(
                htlcResource,
                (exception, result) -> {
                    if (exception != null) {

                        if (exception.getErrorCode()
                                == TransactionException.ErrorCode.ACCOUNT_ERRPR) {
                            /* if has not config chain account for router */
                            logger.warn(
                                    "Failed to get proposalIDs, path: {}, errorMessage: {}, internalMessage: {}",
                                    htlcResource.getSelfPath(),
                                    exception.getLocalizedMessage(),
                                    exception.getInternalMessage());
                        } else {
                            logger.error(
                                    "Failed to get proposalIDs, path: {}, errorMessage: {}, internalMessage: {}",
                                    htlcResource.getSelfPath(),
                                    exception.getLocalizedMessage(),
                                    exception.getInternalMessage());
                        }

                        if (!future.isCancelled()) {
                            future.complete(InterchainDefault.NULL_FLAG);
                        }
                    } else {
                        if (!future.isCancelled()) {
                            future.complete(result);
                        }
                    }
                });

        try {
            String result = future.get(RoutineDefault.CALLBACK_TIMEOUT, TimeUnit.MILLISECONDS);
            if (result == null || RoutineDefault.NULL_FLAG.equals(result)) {
                return new String[] {};
            }
            return result.split(RoutineDefault.SPLIT_REGEX);
        } catch (Exception e) {
            logger.error("Failed to get proposalIDs, path: {}", htlcResource.getSelfPath(), e);
            future.cancel(true);
            return new String[] {};
        }
    }
}
