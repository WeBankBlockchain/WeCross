package com.webank.wecross.routine.xa;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.TransactionRequest;
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

    public interface Callback {
        public void onResponse(Exception e, int result);
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

    private Callback getReduceCallback(int size, Callback callback) {
        AtomicInteger finished = new AtomicInteger();
        List<Exception> fatals = Collections.synchronizedList(new LinkedList<Exception>());

        Callback reduceCallback =
                (error, result) -> {
                    if (error != null) {
                        logger.error("process error!", error);
                        fatals.add(error);
                    }

                    int current = finished.addAndGet(1);
                    if (current == size) {
                        // all finished
                        if (fatals.size() > 0) {
                            logger.error("failed in progress", fatals);
                            StringBuffer errorMsg = new StringBuffer("error occurred,");
                            for (Exception e : fatals) {
                                errorMsg.append(" " + "[" + e.getMessage() + "]");
                            }
                            callback.onResponse(new Exception(errorMsg.toString()), -1);
                        } else {
                            callback.onResponse(null, 0);
                        }
                    }
                };

        return reduceCallback;
    }

    public void asyncStartTransaction(
            String transactionID,
            Map<String, Account> accounts,
            Set<Path> resources,
            Callback callback) {
        try {
            logger.info("startTransaction, resources: {}", resources);

            Map<String, Set<Path>> zone2Path = getChainPaths(resources);

            Callback reduceCallback = getReduceCallback(zone2Path.size(), callback);

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
                proxyPath.setResource("WeCrossProxy");
                Resource resource = zoneManager.fetchResource(proxyPath);

                Account account = accounts.get(resource.getStubType());
                if (Objects.isNull(account)) {
                    String errorMsg =
                            "account with type '" + resource.getStubType() + "' not found";
                    logger.error(errorMsg);
                    reduceCallback.onResponse(new Exception(errorMsg), -1);
                    return;
                }

                resource.asyncSendTransaction(
                        transactionRequest,
                        account,
                        (error, response) -> {
                            if (error != null && !error.isSuccess()) {
                                logger.error("startTransaction failed, ", error);

                                reduceCallback.onResponse(error, -1);
                                return;
                            }

                            if (response.getErrorCode() != 0) {
                                logger.error(
                                        "startTransaction failed: {} {}",
                                        response.getErrorCode(),
                                        response.getErrorMessage());

                                reduceCallback.onResponse(
                                        new Exception(response.getErrorMessage()), -1);
                                return;
                            }

                            int errorCode = Integer.parseInt(response.getResult()[0]);
                            if (errorCode != 0) {
                                logger.error("startTransaction failed: {}", errorCode);
                            }

                            reduceCallback.onResponse(null, errorCode);
                        });
            }
        } catch (Exception e) {
            logger.error("startTransaction error", e);

            callback.onResponse(new Exception("undefined error"), -1);
        }
    };

    public void asyncCommitTransaction(
            String transactionID,
            Map<String, Account> accounts,
            Set<Path> chains,
            Callback callback) {
        try {
            logger.info("commitTransaction, chains: {}", chains);

            Callback reduceCallback = getReduceCallback(chains.size(), callback);

            for (Path chainPath : chains) {
                // send prepare transaction
                TransactionRequest transactionRequest = new TransactionRequest();
                transactionRequest.setMethod("commitTransaction");
                String[] args = new String[] {transactionID};
                transactionRequest.setArgs(args);
                transactionRequest.getOptions().put(Resource.RAW_TRANSACTION, true);

                Path proxyPath = new Path(chainPath);
                proxyPath.setResource("WeCrossProxy");
                Resource resource = zoneManager.fetchResource(proxyPath);

                Account account = accounts.get(resource.getStubType());
                if (Objects.isNull(account)) {
                    String errorMsg =
                            "account with type '" + resource.getStubType() + "' not found";
                    logger.error(errorMsg);
                    reduceCallback.onResponse(new Exception(errorMsg), -1);
                    return;
                }

                resource.asyncSendTransaction(
                        transactionRequest,
                        account,
                        (error, response) -> {
                            if (error != null && !error.isSuccess()) {
                                logger.error("commitTransaction failed, ", error);

                                reduceCallback.onResponse(error, -1);
                                return;
                            }

                            if (response.getErrorCode() != 0) {
                                logger.error(
                                        "commitTransaction failed: {} {}",
                                        response.getErrorCode(),
                                        response.getErrorMessage());

                                reduceCallback.onResponse(
                                        new Exception(response.getErrorMessage()), -1);
                                return;
                            }

                            int errorCode = Integer.parseInt(response.getResult()[0]);
                            if (errorCode != 0) {
                                logger.error("commitTransaction failed: {}", errorCode);
                            }

                            reduceCallback.onResponse(null, errorCode);
                        });
            }
        } catch (Exception e) {
            logger.error("commitTransaction error", e);

            callback.onResponse(new Exception("undefined error"), -1);
        }
    };

    public void asyncRollback(
            String transactionID,
            Map<String, Account> accounts,
            Set<Path> chains,
            Callback callback) {
        try {
            Callback reduceCallback = getReduceCallback(chains.size(), callback);

            for (Path chainPath : chains) {
                // send rollback transaction
                TransactionRequest transactionRequest = new TransactionRequest();
                transactionRequest.setMethod("rollbackTransaction");
                String[] args = new String[] {transactionID};
                transactionRequest.setArgs(args);
                transactionRequest.getOptions().put(Resource.RAW_TRANSACTION, true);

                Path proxyPath = new Path(chainPath);
                proxyPath.setResource("WeCrossProxy");
                Resource resource = zoneManager.fetchResource(proxyPath);

                Account account = accounts.get(resource.getStubType());
                if (Objects.isNull(account)) {
                    String errorMsg =
                            "account with type '" + resource.getStubType() + "' not found";
                    logger.error(errorMsg);
                    reduceCallback.onResponse(new Exception(errorMsg), -1);
                    return;
                }

                resource.asyncSendTransaction(
                        transactionRequest,
                        account,
                        (error, response) -> {
                            if (error != null && !error.isSuccess()) {
                                logger.error("rollbackTransaction failed, ", error);

                                reduceCallback.onResponse(error, -1);
                                return;
                            }

                            if (response.getErrorCode() != 0) {
                                logger.error(
                                        "rollbackTransaction failed: {} {}",
                                        response.getErrorCode(),
                                        response.getErrorMessage());

                                reduceCallback.onResponse(
                                        new Exception(response.getErrorMessage()), -1);
                                return;
                            }

                            int errorCode = Integer.parseInt(response.getResult()[0]);
                            if (errorCode != 0) {
                                logger.error("rollbackTransaction failed: {}", errorCode);
                            }

                            reduceCallback.onResponse(null, errorCode);
                        });
            }
        } catch (Exception e) {
            logger.error("rollbackTransaction error", e);

            callback.onResponse(new Exception("undefined error"), -1);
        }
    }

    public interface GetTransactionInfoCallback {
        public void onResponse(Exception e, XATransactionInfo xaTransactionInfo);
    }

    private GetTransactionInfoCallback getTransactionInfoReduceCallback(
            int size, GetTransactionInfoCallback callback) {
        AtomicInteger finished = new AtomicInteger();
        List<Exception> fatals = Collections.synchronizedList(new LinkedList<Exception>());
        List<XATransactionInfo> infos =
                Collections.synchronizedList(new LinkedList<XATransactionInfo>());

        GetTransactionInfoCallback reduceCallback =
                (error, info) -> {
                    if (error != null) {
                        logger.error("getTransactionInfo error in progress", error);
                        fatals.add(error);
                    } else {
                        infos.add(info);
                    }

                    int current = finished.addAndGet(1);
                    if (current == size) {
                        if (infos.isEmpty()) {
                            logger.error("empty result");
                            callback.onResponse(new WeCrossException(-1, "empty result"), null);

                            return;
                        }

                        if (infos.size() < size) {
                            logger.warn("getTransactionInfo missing some steps");
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
            Map<String, Account> accounts,
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
                proxyPath.setResource("WeCrossProxy");
                Resource resource = zoneManager.fetchResource(proxyPath);

                Account account = accounts.get(resource.getStubType());
                if (Objects.isNull(account)) {
                    String errorMsg =
                            "account with type '" + resource.getStubType() + "' not found";
                    logger.error(errorMsg);
                    reduceCallback.onResponse(new Exception(errorMsg), null);
                    return;
                }

                resource.asyncCall(
                        transactionRequest,
                        account,
                        (error, response) -> {
                            if (error != null && !error.isSuccess()) {
                                logger.error("getTransactionInfo error", error);

                                reduceCallback.onResponse(
                                        new WeCrossException(-1, "getTransactionInfo error", error),
                                        null);
                                return;
                            }

                            // decode return value
                            try {
                                // remove unseem char
                                String rawJSON = response.getResult()[0];

                                if (logger.isDebugEnabled()) {
                                    logger.debug("transaction info: {}", rawJSON);
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
                                logger.error("decode transactionInfo json error", e);

                                reduceCallback.onResponse(
                                        new WeCrossException(
                                                -1, "decode transactionInfo json error", e),
                                        null);
                                return;
                            }
                        });
            }
        } catch (Exception e) {
            logger.error("getTransactionInfo error", e);

            callback.onResponse(new WeCrossException(-1, "getTransactionInfo error", e), null);
        }
    }

    public ZoneManager getZoneManager() {
        return zoneManager;
    }

    public void setZoneManager(ZoneManager zoneManager) {
        this.zoneManager = zoneManager;
    }
}
