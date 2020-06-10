package com.webank.wecross.routine.xa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.zone.Chain;
import com.webank.wecross.zone.ZoneManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XATransactionManager {
    private Logger logger = LoggerFactory.getLogger(XATransactionManager.class);
    ObjectMapper objectMapper = new ObjectMapper();
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

    public void asyncPrepare(
            String transactionID, Account account, Set<Path> resources, Callback callback) {
        try {
            Map<String, Set<Path>> zone2Path = getChainPaths(resources);

            for (Map.Entry<String, Set<Path>> entry : zone2Path.entrySet()) {
                Path chainPath = entry.getValue().iterator().next();

                Chain chain = zoneManager.getZone(chainPath).getChain(chainPath);

                // send prepare transaction
                TransactionRequest transactionRequest = new TransactionRequest();
                transactionRequest.setMethod("startTransaction");
                String[] args = new String[entry.getValue().size() + 1];
                int i = 0;
                args[0] = transactionID;
                for (Path path : entry.getValue()) {
                    args[i + 1] = path.toString();
                    ++i;
                }
                transactionRequest.setArgs(args);

                Path proxyPath = new Path(chainPath);
                proxyPath.setResource("WeCrossProxy");
                Resource resource = zoneManager.getResource(proxyPath);

                TransactionContext<TransactionRequest> transactionContext =
                        new TransactionContext<TransactionRequest>(
                                transactionRequest,
                                account,
                                resource.getResourceInfo(),
                                chain.getBlockHeaderManager());

                resource.asyncSendTransaction(
                        transactionContext,
                        (error, response) -> {
                            if (error != null) {
                                logger.error("Send prepare transaction system error", error);

                                callback.onResponse(error, -1);
                                return;
                            }

                            int errorCode = Integer.parseInt(response.getResult()[0]);
                            if (errorCode != 0) {
                                logger.error("Send prepare transaction proxy error: {}", errorCode);
                            }

                            callback.onResponse(null, errorCode);
                        });
            }
        } catch (Exception e) {
            logger.error("Prepare error", e);

            callback.onResponse(new WeCrossException(-1, "Prepare error", e), -1);
        }
    };

    public void asyncCommit(
            String transactionID, Account account, Set<Path> chains, Callback callback) {
        try {
            for (Path chainPath : chains) {
                Chain chain = zoneManager.getZone(chainPath).getChain(chainPath);

                // send prepare transaction
                TransactionRequest transactionRequest = new TransactionRequest();
                transactionRequest.setMethod("commitTransaction");
                String[] args = new String[] {transactionID};
                transactionRequest.setArgs(args);

                Path proxyPath = new Path(chainPath);
                proxyPath.setResource("WeCrossProxy");
                Resource resource = zoneManager.getResource(proxyPath);

                TransactionContext<TransactionRequest> transactionContext =
                        new TransactionContext<TransactionRequest>(
                                transactionRequest,
                                account,
                                resource.getResourceInfo(),
                                chain.getBlockHeaderManager());

                resource.asyncSendTransaction(
                        transactionContext,
                        (error, response) -> {
                            if (error != null) {
                                logger.error("Send commit transaction error", error);

                                callback.onResponse(
                                        new WeCrossException(
                                                -1, "Send commit transaction error", error),
                                        -1);
                                return;
                            }

                            int errorCode = Integer.parseInt(response.getResult()[0]);
                            if (errorCode != 0) {
                                logger.error("Send prepare transaction proxy error: {}", errorCode);
                            }

                            callback.onResponse(null, errorCode);
                        });
            }
        } catch (Exception e) {
            logger.error("Commit error", e);

            callback.onResponse(new WeCrossException(-1, "Commit error", e), -1);
        }
    };

    public void asyncRollback(
            String transactionID, Account account, Set<Path> chains, Callback callback) {
        try {
            for (Path chainPath : chains) {
                Chain chain = zoneManager.getZone(chainPath).getChain(chainPath);

                // send rollback transaction
                TransactionRequest transactionRequest = new TransactionRequest();
                transactionRequest.setMethod("rollbackTransaction");
                String[] args = new String[] {transactionID};
                transactionRequest.setArgs(args);

                Path proxyPath = new Path(chainPath);
                proxyPath.setResource("WeCrossProxy");
                Resource resource = zoneManager.getResource(proxyPath);

                TransactionContext<TransactionRequest> transactionContext =
                        new TransactionContext<TransactionRequest>(
                                transactionRequest,
                                account,
                                resource.getResourceInfo(),
                                chain.getBlockHeaderManager());

                resource.asyncSendTransaction(
                        transactionContext,
                        (error, response) -> {
                            if (error != null) {
                                logger.error("Send rollback transaction error", error);

                                callback.onResponse(
                                        new WeCrossException(
                                                -1, "Send rollback transaction error", error),
                                        -1);
                                return;
                            }

                            int errorCode = Integer.parseInt(response.getResult()[0]);
                            if (errorCode != 0) {
                                logger.error("Send prepare transaction proxy error: {}", errorCode);
                            }

                            callback.onResponse(null, errorCode);
                        });
            }
        } catch (Exception e) {
            logger.error("Rollback error", e);

            callback.onResponse(new WeCrossException(-1, "Rollback error", e), -1);
        }
    }

    public interface GetTransactionInfoCallback {
        public void onResponse(Exception e, XATransactionInfo xaTransactionInfo);
    }

    public void asyncGetTransactionInfo(
            String transactionID, Path path, Account account, GetTransactionInfoCallback callback) {
        try {
            Chain chain = zoneManager.getZone(path).getChain(path);

            TransactionRequest transactionRequest = new TransactionRequest();
            transactionRequest.setMethod("getTransactionInfo");
            String[] args = new String[] {transactionID};
            transactionRequest.setArgs(args);

            Path proxyPath = new Path(path);
            proxyPath.setResource("WeCrossProxy");
            Resource resource = zoneManager.getResource(proxyPath);

            TransactionContext<TransactionRequest> transactionContext =
                    new TransactionContext<TransactionRequest>(
                            transactionRequest,
                            account,
                            resource.getResourceInfo(),
                            chain.getBlockHeaderManager());

            resource.asyncCall(
                    transactionContext,
                    (error, response) -> {
                        if (error != null) {
                            logger.error("Send prepare transaction error", error);

                            callback.onResponse(
                                    new WeCrossException(-1, "GetTransactionInfo error", error),
                                    null);
                            return;
                        }

                        // decode return value
                        try {
                            XATransactionInfo xaTransactionInfo =
                                    objectMapper.readValue(
                                            response.getResult()[0], XATransactionInfo.class);

                            callback.onResponse(null, xaTransactionInfo);
                        } catch (Exception e) {
                            logger.error("Decode transactionInfo json error", e);

                            callback.onResponse(
                                    new WeCrossException(
                                            -1, "Decode transactionInfo json error", e),
                                    null);
                            return;
                        }
                    });
        } catch (Exception e) {
            logger.error("GetTransactionInfo error", e);

            callback.onResponse(new WeCrossException(-1, "GetTransactionInfo error", e), null);
        }
    }

    public ZoneManager getZoneManager() {
        return zoneManager;
    }

    public void setZoneManager(ZoneManager zoneManager) {
        this.zoneManager = zoneManager;
    }
}
