package com.webank.wecross.routine.xa;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.polling.TaskManager;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.*;
import com.webank.wecross.zone.ZoneManager;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XATransactionManager {
    private Logger logger = LoggerFactory.getLogger(XATransactionManager.class);
    ObjectMapper objectMapper =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private ZoneManager zoneManager;
    private static final int GET_ALL = 0;
    private static final int GET_FINISHED = 1;
    private static final int GET_UNFINISHED = 2;

    public interface ReduceCallback {
        void onResponse(WeCrossException e, int result);
    }

    public Map<String, Set<Path>> getChainPaths(Set<Path> resources) {
        Map<String, Set<Path>> zone2Path = new HashMap<String, Set<Path>>();
        for (Path path : resources) {
            String key = path.getZone() + "." + path.getChain();
            if (zone2Path.get(key) == null) {
                zone2Path.put(key, new HashSet<Path>());
            }

            zone2Path.get(key).add(path);
        }

        return zone2Path;
    }

    private ReduceCallback getReduceCallback(int size, ReduceCallback callback) {
        AtomicInteger finished = new AtomicInteger();
        List<WeCrossException> fatals = Collections.synchronizedList(new LinkedList<>());

        ReduceCallback reduceCallback =
                (exception, result) -> {
                    if (exception != null) {
                        logger.error("Process error!", exception);
                        fatals.add(exception);
                    }

                    int current = finished.addAndGet(1);
                    if (current == size) {
                        // all finished
                        if (fatals.size() > 0) {
                            logger.error("Failed in progress", fatals);
                            StringBuffer errorMsg = new StringBuffer("Error occurred,");
                            for (Exception e : fatals) {
                                errorMsg.append(" " + "[" + e.getMessage() + "]");
                            }
                            callback.onResponse(
                                    new WeCrossException(
                                            fatals.get(0).getErrorCode(), errorMsg.toString()),
                                    -1);
                        } else {
                            callback.onResponse(null, 0);
                        }
                    }
                };

        return reduceCallback;
    }

    public void asyncStartTransaction(
            String transactionID,
            UniversalAccount ua,
            Set<Path> resources,
            ReduceCallback callback) {
        try {
            logger.info("StartTransaction, resources: {}", resources);

            Map<String, Set<Path>> zone2Path = getChainPaths(resources);

            ReduceCallback reduceCallback = getReduceCallback(zone2Path.size(), callback);

            for (Map.Entry<String, Set<Path>> entry : zone2Path.entrySet()) {
                Path chainPath = entry.getValue().iterator().next();
                // send prepare transaction
                TransactionRequest transactionRequest = new TransactionRequest();
                transactionRequest.setMethod("startTransaction");
                String[] args = new String[entry.getValue().size() + resources.size() + 2];

                args[0] = transactionID;
                args[1] = String.valueOf(entry.getValue().size());

                int i = 0;
                for (Path path : entry.getValue()) {
                    args[i + 2] = path.toString();
                    ++i;
                }

                for (Path path : resources) {
                    args[i + 2] = path.toString();
                    ++i;
                }

                transactionRequest.setArgs(args);
                transactionRequest.getOptions().put(Resource.RAW_TRANSACTION, true);

                Path proxyPath = new Path(chainPath);
                proxyPath.setResource(StubConstant.PROXY_NAME);
                Resource resource = zoneManager.fetchResource(proxyPath);

                resource.asyncSendTransaction(
                        transactionRequest,
                        ua,
                        (exception, response) -> {
                            if (exception != null && !exception.isSuccess()) {
                                logger.error("StartTransaction failed, ", exception);

                                reduceCallback.onResponse(
                                        new WeCrossException(
                                                XAErrorCode.START_TRANSACTION_ERROR,
                                                exception.getMessage()),
                                        -1);
                                return;
                            }

                            if (response.getErrorCode() != 0) {
                                logger.error(
                                        "StartTransaction failed: {} {}",
                                        response.getErrorCode(),
                                        response.getErrorMessage());

                                reduceCallback.onResponse(
                                        new WeCrossException(
                                                XAErrorCode.START_TRANSACTION_ERROR,
                                                response.getErrorMessage()),
                                        -1);
                                return;
                            }

                            int errorCode = Integer.parseInt(response.getResult()[0]);
                            if (errorCode != 0) {
                                logger.error("StartTransaction failed: {}", errorCode);
                            }

                            reduceCallback.onResponse(null, errorCode);
                        });
            }
        } catch (Exception e) {
            logger.error("StartTransaction error", e);
            callback.onResponse(
                    new WeCrossException(XAErrorCode.UNDEFINED_ERROR, "Undefined error"), -1);
        }
    }

    public void asyncCommitTransaction(
            String transactionID, UniversalAccount ua, Set<Path> chains, ReduceCallback callback) {
        try {
            logger.info("CommitTransaction, chains: {}", chains);

            ReduceCallback reduceCallback = getReduceCallback(chains.size(), callback);

            for (Path chainPath : chains) {
                // send prepare transaction
                TransactionRequest transactionRequest = new TransactionRequest();
                transactionRequest.setMethod("commitTransaction");
                String[] args = new String[] {transactionID};
                transactionRequest.setArgs(args);
                transactionRequest.getOptions().put(Resource.RAW_TRANSACTION, true);

                Path proxyPath = new Path(chainPath);
                proxyPath.setResource(StubConstant.PROXY_NAME);
                Resource resource = zoneManager.fetchResource(proxyPath);

                resource.asyncSendTransaction(
                        transactionRequest,
                        ua,
                        (exception, response) -> {
                            if (exception != null && !exception.isSuccess()) {
                                logger.error("CommitTransaction failed, ", exception);

                                reduceCallback.onResponse(
                                        new WeCrossException(
                                                XAErrorCode.COMMIT_TRANSACTION_ERROR,
                                                exception.getMessage()),
                                        -1);
                                return;
                            }

                            if (response.getErrorCode() != 0) {
                                logger.error(
                                        "CommitTransaction failed: {} {}",
                                        response.getErrorCode(),
                                        response.getErrorMessage());

                                reduceCallback.onResponse(
                                        new WeCrossException(
                                                XAErrorCode.COMMIT_TRANSACTION_ERROR,
                                                response.getErrorMessage()),
                                        -1);
                                return;
                            }

                            int errorCode = Integer.parseInt(response.getResult()[0]);
                            if (errorCode != 0) {
                                logger.error("CommitTransaction failed: {}", errorCode);
                            }

                            reduceCallback.onResponse(null, errorCode);
                        });
            }
        } catch (Exception e) {
            logger.error("CommitTransaction error", e);
            callback.onResponse(
                    new WeCrossException(XAErrorCode.UNDEFINED_ERROR, "Undefined error"), -1);
        }
    };

    public void asyncRollback(
            String transactionID, UniversalAccount ua, Set<Path> chains, ReduceCallback callback) {
        try {
            ReduceCallback reduceCallback = getReduceCallback(chains.size(), callback);

            for (Path chainPath : chains) {
                // send rollback transaction
                TransactionRequest transactionRequest = new TransactionRequest();
                transactionRequest.setMethod("rollbackTransaction");
                String[] args = new String[] {transactionID};
                transactionRequest.setArgs(args);
                transactionRequest.getOptions().put(Resource.RAW_TRANSACTION, true);

                Path proxyPath = new Path(chainPath);
                proxyPath.setResource(StubConstant.PROXY_NAME);
                Resource resource = zoneManager.fetchResource(proxyPath);

                resource.asyncSendTransaction(
                        transactionRequest,
                        ua,
                        (exception, response) -> {
                            if (exception != null && !exception.isSuccess()) {
                                logger.error("RollbackTransaction failed, ", exception);

                                reduceCallback.onResponse(
                                        new WeCrossException(
                                                XAErrorCode.ROLLBACK_TRANSACTION_ERROR,
                                                exception.getMessage()),
                                        -1);
                                return;
                            }

                            if (response.getErrorCode() != 0) {
                                logger.error(
                                        "RollbackTransaction failed: {} {}",
                                        response.getErrorCode(),
                                        response.getErrorMessage());

                                reduceCallback.onResponse(
                                        new WeCrossException(
                                                XAErrorCode.ROLLBACK_TRANSACTION_ERROR,
                                                response.getErrorMessage()),
                                        -1);
                                return;
                            }

                            int errorCode = Integer.parseInt(response.getResult()[0]);
                            if (errorCode != 0) {
                                logger.error("RollbackTransaction failed: {}", errorCode);
                            }

                            reduceCallback.onResponse(null, errorCode);
                        });
            }
        } catch (Exception e) {
            logger.error("RollbackTransaction error", e);
            callback.onResponse(
                    new WeCrossException(XAErrorCode.UNDEFINED_ERROR, "Undefined error"), -1);
        }
    }

    public interface GetTransactionInfoCallback {
        void onResponse(WeCrossException e, XATransactionInfo xaTransactionInfo);
    }

    private GetTransactionInfoCallback getTransactionInfoReduceCallback(
            int size, GetTransactionInfoCallback callback) {
        AtomicInteger finished = new AtomicInteger();
        List<WeCrossException> fatals = Collections.synchronizedList(new LinkedList<>());
        List<XATransactionInfo> infos = Collections.synchronizedList(new LinkedList<>());

        GetTransactionInfoCallback reduceCallback =
                (exception, info) -> {
                    if (exception != null) {
                        logger.error("GetTransactionInfo error in progress", exception);
                        fatals.add(exception);
                    } else {
                        infos.add(info);
                    }

                    int current = finished.addAndGet(1);
                    if (current == size) {
                        if (infos.isEmpty()) {
                            logger.error("Empty result");
                            callback.onResponse(
                                    new WeCrossException(
                                            XAErrorCode.GET_TRANSACTION_INFO_ERROR, "Empty result"),
                                    null);

                            return;
                        }

                        if (infos.size() < size) {
                            logger.warn("GetTransactionInfo missing some steps");
                        }

                        List<XATransactionStep> allSteps = new ArrayList<XATransactionStep>();
                        for (XATransactionInfo returnInfo : infos) {
                            allSteps.addAll(returnInfo.getTransactionSteps());
                        }

                        allSteps.sort(
                                (left, right) -> {
                                    return Integer.compare(left.getSeq(), right.getSeq());
                                });

                        XATransactionInfo xaTransactionInfo = infos.get(0);
                        xaTransactionInfo.setTransactionSteps(allSteps);

                        callback.onResponse(null, xaTransactionInfo);
                    }
                };

        return reduceCallback;
    }

    public void asyncGetTransactionInfo(
            String transactionID,
            UniversalAccount ua,
            Set<Path> chains,
            GetTransactionInfoCallback callback) {
        try {
            GetTransactionInfoCallback reduceCallback =
                    getTransactionInfoReduceCallback(chains.size(), callback);

            for (Path path : chains) {
                TransactionRequest transactionRequest = new TransactionRequest();
                transactionRequest.setMethod("getTransactionInfo");
                String[] args = new String[] {transactionID};
                transactionRequest.setArgs(args);
                transactionRequest.getOptions().put(Resource.RAW_TRANSACTION, true);

                Path proxyPath = new Path(path);
                proxyPath.setResource(StubConstant.PROXY_NAME);
                Resource resource = zoneManager.fetchResource(proxyPath);

                resource.asyncCall(
                        transactionRequest,
                        ua,
                        (exception, response) -> {
                            if (exception != null && !exception.isSuccess()) {
                                logger.error("GetTransactionInfo error", exception);

                                reduceCallback.onResponse(
                                        new WeCrossException(
                                                XAErrorCode.GET_TRANSACTION_INFO_ERROR,
                                                exception.getMessage()),
                                        null);
                                return;
                            }

                            if (response.getErrorCode() != 0) {
                                logger.error(
                                        "GetTransactionInfo failed: {} {}",
                                        response.getErrorCode(),
                                        response.getErrorMessage());

                                reduceCallback.onResponse(
                                        new WeCrossException(
                                                XAErrorCode.GET_TRANSACTION_INFO_ERROR,
                                                response.getErrorMessage()),
                                        null);
                                return;
                            }

                            // decode return value
                            try {
                                // remove unseem char
                                String rawJSON = response.getResult()[0];

                                if (logger.isDebugEnabled()) {
                                    logger.debug("Transaction info: {}", rawJSON);
                                }

                                StringBuffer buffer = new StringBuffer();
                                for (int i = 0; i < rawJSON.length(); ++i) {
                                    char c = rawJSON.charAt(i);
                                    if (c != 0x0) {
                                        buffer.append(c);
                                    }
                                }

                                XATransactionInfo xaTransactionInfo =
                                        objectMapper.readValue(
                                                buffer.toString(), XATransactionInfo.class);

                                reduceCallback.onResponse(null, xaTransactionInfo);
                            } catch (Exception e) {
                                logger.error("Decode transactionInfo json error", e);

                                reduceCallback.onResponse(
                                        new WeCrossException(
                                                -1, "Decode transactionInfo json error", e),
                                        null);
                                return;
                            }
                        });
            }
        } catch (Exception e) {
            logger.error("GetTransactionInfo error", e);
            callback.onResponse(
                    new WeCrossException(XAErrorCode.UNDEFINED_ERROR, "Undefined error"), null);
        }
    }

    public interface GetTransactionIDsCallback {
        void onResponse(WeCrossException e, String[] result);
    }

    public void asyncGetTransactionIDs(
            Resource proxyResource,
            UniversalAccount ua,
            int option,
            GetTransactionIDsCallback callback) {
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.getOptions().put(Resource.RAW_TRANSACTION, true);

        try {
            if (option == GET_ALL || option == GET_FINISHED) {
                if (option == GET_ALL) {
                    transactionRequest.setMethod("getAllTransactionIDs");
                } else {
                    transactionRequest.setMethod("getFinishedTransactionIDs");
                }

                proxyResource.asyncCall(
                        transactionRequest,
                        ua,
                        (exception, response) -> {
                            if (exception != null && !exception.isSuccess()) {
                                callback.onResponse(
                                        new WeCrossException(
                                                XAErrorCode.GET_TRANSACTION_IDS_ERROR,
                                                exception.getMessage()),
                                        null);
                                return;
                            }

                            if (response.getErrorCode() != 0) {
                                callback.onResponse(
                                        new WeCrossException(
                                                XAErrorCode.GET_TRANSACTION_IDS_ERROR,
                                                response.getErrorMessage()),
                                        null);
                                return;
                            }
                            callback.onResponse(null, response.getResult()[0].trim().split(" "));
                        });
            } else if (option == GET_UNFINISHED) {
                asyncGetUnfinishedTransactionIDs(proxyResource, ua, callback);
            } else {
                logger.error("GetTransactionIDs error, undefined option: {}", option);
                callback.onResponse(
                        new WeCrossException(
                                XAErrorCode.GET_TRANSACTION_IDS_ERROR,
                                "Undefined option: " + option),
                        null);
            }
        } catch (Exception e) {
            logger.error("GetTransactionIDs error", e);
            callback.onResponse(
                    new WeCrossException(XAErrorCode.UNDEFINED_ERROR, "Undefined error"), null);
        }
    }

    public void asyncGetUnfinishedTransactionIDs(
            Resource proxyResource, UniversalAccount ua, GetTransactionIDsCallback callback) {
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.getOptions().put(Resource.RAW_TRANSACTION, true);
        // get all ids
        transactionRequest.setMethod("getAllTransactionIDs");
        proxyResource.asyncCall(
                transactionRequest,
                ua,
                (exception, response) -> {
                    if (exception != null && !exception.isSuccess()) {
                        callback.onResponse(
                                new WeCrossException(
                                        XAErrorCode.GET_TRANSACTION_IDS_ERROR,
                                        exception.getMessage()),
                                null);
                        return;
                    }

                    if (response.getErrorCode() != 0) {
                        callback.onResponse(
                                new WeCrossException(
                                        XAErrorCode.GET_TRANSACTION_IDS_ERROR,
                                        response.getErrorMessage()),
                                null);
                        return;
                    }

                    // get finished ids
                    transactionRequest.setMethod("getFinishedTransactionIDs");
                    proxyResource.asyncCall(
                            transactionRequest,
                            ua,
                            (exception1, response1) -> {
                                if (exception1 != null && !exception1.isSuccess()) {
                                    callback.onResponse(
                                            new WeCrossException(
                                                    XAErrorCode.GET_TRANSACTION_IDS_ERROR,
                                                    exception1.getMessage()),
                                            null);
                                    return;
                                }

                                if (response1.getErrorCode() != 0) {
                                    callback.onResponse(
                                            new WeCrossException(
                                                    XAErrorCode.GET_TRANSACTION_IDS_ERROR,
                                                    response1.getErrorMessage()),
                                            null);
                                    return;
                                }

                                Set<String> allIDs =
                                        new HashSet<>(
                                                Arrays.asList(
                                                        response.getResult()[0].trim().split(" ")));
                                Set<String> finishedIDs =
                                        new HashSet<>(
                                                Arrays.asList(
                                                        response1
                                                                .getResult()[0]
                                                                .trim()
                                                                .split(" ")));
                                allIDs.removeAll(finishedIDs);

                                callback.onResponse(null, allIDs.toArray(new String[0]));
                            });
                });
    }

    public void registerTask(TaskManager taskManager) {}

    public ZoneManager getZoneManager() {
        return zoneManager;
    }

    public void setZoneManager(ZoneManager zoneManager) {
        this.zoneManager = zoneManager;
    }
}
