package com.webank.wecross.interchain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.account.UniversalAccount;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.routine.RoutineDefault;
import com.webank.wecross.stub.ObjectMapperFactory;
import com.webank.wecross.stub.TransactionException;
import com.webank.wecross.stub.TransactionRequest;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisallowConcurrentExecution
public class InterchainJob implements Job {
    private Logger logger = LoggerFactory.getLogger(InterchainJob.class);
    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        SystemResource systemResource =
                (SystemResource) dataMap.get(InterchainDefault.INTER_CHAIN_JOB_DATA_KEY);

        handleInterchainRequests(systemResource);
    }

    public void handleInterchainRequests(SystemResource systemResource) {

        UniversalAccount adminUA;
        try {
            adminUA = systemResource.getAccountManager().getAdminUA();
        } catch (WeCrossException e) {
            logger.error("getAdminUA failed: ", e);
            return;
        }
        String[] requests = getInterchainRequests(systemResource, adminUA);
        if (Objects.nonNull(requests)) {

            Semaphore semaphore = new Semaphore(requests.length, true);
            try {
                semaphore.acquire(requests.length);
            } catch (InterruptedException e) {
                logger.warn("Interrupted: ", e);
                Thread.currentThread().interrupt();
            }

            AtomicInteger count = new AtomicInteger(0);
            String currentIndex = "0";

            String hubPath = systemResource.getHubResource().getPath().toString();
            for (String request : requests) {
                try {
                    InterchainRequest interchainRequest = new InterchainRequest();
                    interchainRequest.build(request);
                    if (count.getAndIncrement() == requests.length - 1) {
                        currentIndex = interchainRequest.getUid();
                    }

                    logger.info("Start handle inter chain request: {}, path: {}", request, hubPath);

                    UniversalAccount userUA =
                            systemResource
                                    .getAccountManager()
                                    .getUniversalAccountByIdentity(interchainRequest.getIdentity());

                    InterchainScheduler interchainScheduler = new InterchainScheduler();
                    interchainScheduler.setInterchainRequest(interchainRequest);
                    interchainScheduler.setSystemResource(systemResource);
                    interchainScheduler.setAdminUA(adminUA);
                    interchainScheduler.setUserUA(userUA);

                    interchainScheduler.start(
                            (exception) -> {
                                if (Objects.nonNull(exception)) {
                                    logger.error(
                                            "Failed to handle current inter chain request: {}, path: {}, errorMessage: {}, internalMessage: {}",
                                            request,
                                            hubPath,
                                            exception.getLocalizedMessage(),
                                            exception.getInternalMessage());
                                }
                                semaphore.release();
                            });
                } catch (WeCrossException e) {
                    logger.error(
                            "Failed to handle current inter chain request: {}, path: {}, errorMessage: {}, internalMessage: {}",
                            request,
                            hubPath,
                            e.getLocalizedMessage(),
                            e.getInternalMessage());
                    semaphore.release();
                }
            }

            try {
                // wait until job is finished
                semaphore.acquire(requests.length);
            } catch (InterruptedException e) {
                logger.warn("Interrupted,", e);
                Thread.currentThread().interrupt();
            }

            // update request index
            if (!"0".equals(currentIndex)) {
                updateCurrentRequestIndex(systemResource, currentIndex, adminUA);
            }
        }
    }

    public String[] getInterchainRequests(SystemResource systemResource, UniversalAccount ua) {
        Resource hubResource = systemResource.getHubResource();
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setArgs(new String[] {InterchainDefault.MAX_NUMBER_PER_POLLING});
        transactionRequest.setMethod(InterchainDefault.GET_INTER_CHAIN_REQUESTS_METHOD);

        CompletableFuture<String> future = new CompletableFuture<>();
        hubResource.asyncCall(
                transactionRequest,
                ua,
                (transactionException, transactionResponse) -> {
                    if (Objects.nonNull(transactionException)
                            && !transactionException.isSuccess()) {
                        if (transactionException.getErrorCode()
                                == TransactionException.ErrorCode.ACCOUNT_ERRPR) {
                            /* if has not config chain account for router */
                            logger.warn(
                                    "Failed to get interchain requests, path: {}, errorMessage: {}",
                                    hubResource.getPath(),
                                    transactionException.getMessage());
                        } else {
                            logger.error(
                                    "Failed to get interchain requests, path: {}, errorMessage: {}",
                                    hubResource.getPath(),
                                    transactionException.getMessage());
                        }

                        if (!future.isCancelled()) {
                            future.complete(InterchainDefault.NULL_FLAG);
                        }
                    } else if (transactionResponse.getErrorCode() != 0) {
                        logger.error(
                                "Failed to get interchain requests, path: {}, errorMessage: {}",
                                hubResource.getPath(),
                                transactionResponse.getMessage());
                        if (!future.isCancelled()) {
                            future.complete(InterchainDefault.NULL_FLAG);
                        }
                    } else {
                        if (Objects.isNull(transactionResponse.getResult())
                                || transactionResponse.getResult().length == 0) {
                            logger.error(
                                    "Failed to get interchain requests, path: {}, no response found",
                                    hubResource.getPath());
                            if (!future.isCancelled()) {
                                future.complete(InterchainDefault.NULL_FLAG);
                            }
                        } else {
                            if (!future.isCancelled()) {
                                future.complete(transactionResponse.getResult()[0]);
                            }
                        }
                    }
                });

        String result = null;
        try {
            result = future.get(RoutineDefault.CALLBACK_TIMEOUT, TimeUnit.MILLISECONDS);
            if (Objects.isNull(result) || RoutineDefault.NULL_FLAG.equals(result)) {
                return new String[] {};
            }

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Interchain json requests: {}, path: {}", result, hubResource.getPath());
            }

            return objectMapper.readValue(
                    result.replace(new String(Character.toChars(0)), ""), String[].class);
        } catch (Exception e) {
            logger.error(
                    "Failed to get interchain requests, result: {} path: {}",
                    result,
                    hubResource.getPath(),
                    e);
            future.cancel(true);
            return new String[] {};
        }
    }

    public void updateCurrentRequestIndex(
            SystemResource systemResource, String index, UniversalAccount ua) {
        Resource hubResource = systemResource.getHubResource();
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setArgs(new String[] {index});
        transactionRequest.setMethod(InterchainDefault.UPDATE_CURRENT_REQUEST_INDEX_METHOD);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "update current request index: {}, path: {}",
                    index,
                    systemResource.getHubResource().getPath());
        }

        Semaphore semaphore = new Semaphore(1, true);
        try {
            semaphore.acquire(1);
        } catch (InterruptedException e) {
            logger.warn("Interrupted: ", e);
            Thread.currentThread().interrupt();
        }
        hubResource.asyncSendTransaction(
                transactionRequest,
                ua,
                (transactionException, transactionResponse) -> {
                    if (Objects.nonNull(transactionException)
                            && !transactionException.isSuccess()) {
                        logger.error(
                                "Failed to update current interchain request index, path: {}, errorMessage: {}",
                                hubResource.getPath(),
                                transactionException.getMessage());
                    } else if (transactionResponse.getErrorCode() != 0) {
                        logger.error(
                                "Failed to update current interchain request index: {}, errorMessage: {}",
                                hubResource.getPath(),
                                transactionResponse.getMessage());
                    } else {
                        logger.info("Current interchain requests finished, index: {}", index);
                    }
                    semaphore.release();
                });

        try {
            // wait until job is finished
            semaphore.acquire(1);
        } catch (InterruptedException e) {
            logger.warn("Interrupted,", e);
            Thread.currentThread().interrupt();
        }
    }
}
