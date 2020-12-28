package com.webank.wecross.routine.xa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.account.AccountManager;
import com.webank.wecross.account.UniversalAccount;
import com.webank.wecross.common.WeCrossDefault;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.polling.TaskManager;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.*;
import com.webank.wecross.zone.ZoneManager;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XATransactionManager {
    private Logger logger = LoggerFactory.getLogger(XATransactionManager.class);
    ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    private ZoneManager zoneManager;
    private AccountManager accountManager;

    private static final String SUCCESS = "Success";

    public interface XAReduceCallback {
        void onResponse(XAResponse xaResponse);
    }

    /**
     * check and reorganize resources by chain type used by startXATransaction forbid system
     * contracts
     */
    public Map<String, Set<String>> getChainPaths(Set<String> resources) throws Exception {
        Map<String, Set<String>> zone2Path = Collections.synchronizedMap(new Hashtable<>());
        for (String resource : resources) {
            Path path;
            try {
                path = Path.decode(resource);
            } catch (Exception e) {
                throw new Exception("Invalid path found");
            }
            if (StubConstant.HUB_NAME.equals(path.getResource())
                    || StubConstant.PROXY_NAME.equals(path.getResource())) {
                throw new Exception("System contracts cannot be in xa transaction");
            }
            String key = path.getZone() + "." + path.getChain();
            zone2Path.computeIfAbsent(key, k -> new HashSet<>());
            zone2Path.get(key).add(resource);
        }
        return zone2Path;
    }

    private List<Path> setToList(Set<String> paths) {
        return paths.parallelStream()
                .map(
                        (s) -> {
                            try {
                                return Path.decode(s);
                            } catch (Exception e) {
                                logger.error("Decode path error: ", e);
                                return null;
                            }
                        })
                .collect(Collectors.toList());
    }

    private XAReduceCallback getXAReduceCallback(int size, XAReduceCallback callback) {
        AtomicInteger finished = new AtomicInteger();
        List<XAResponse.ChainErrorMessage> errors =
                Collections.synchronizedList(new LinkedList<>());

        return (response) -> {
            if (!response.getChainErrorMessages().isEmpty()) {
                errors.add(response.getChainErrorMessages().get(0));
            }

            int current = finished.addAndGet(1);
            if (current == size) {
                // all finished
                if (!errors.isEmpty()) {
                    XAResponse xaResponse = new XAResponse();
                    xaResponse.setStatus(-1);
                    xaResponse.setChainErrorMessages(errors);
                    callback.onResponse(xaResponse);
                } else {
                    callback.onResponse(new XAResponse());
                }
            }
        };
    }

    private XAReduceCallback getStartXAReduceCallback(
            int size,
            String xaTransactionID,
            UniversalAccount ua,
            Set<String> chains,
            XAReduceCallback callback) {
        AtomicInteger finished = new AtomicInteger();
        List<XAResponse.ChainErrorMessage> errors =
                Collections.synchronizedList(new LinkedList<>());

        return (response) -> {
            if (!response.getChainErrorMessages().isEmpty()) {
                errors.add(response.getChainErrorMessages().get(0));
                chains.remove(response.getChainErrorMessages().get(0).getPath());
            }

            int current = finished.addAndGet(1);
            if (current == size) {
                // all finished
                if (!errors.isEmpty()) {
                    // error occurred
                    XAResponse xaResponse = new XAResponse();
                    xaResponse.setStatus(-1);
                    xaResponse.setChainErrorMessages(errors);

                    // commit succeeded chains
                    if (!chains.isEmpty()) {
                        Set<Path> chainPaths =
                                chains.parallelStream()
                                        .map(
                                                (s) -> {
                                                    try {
                                                        return Path.decode(s);
                                                    } catch (Exception e) {
                                                        logger.error("Decode path error: ", e);
                                                        return null;
                                                    }
                                                })
                                        .collect(Collectors.toSet());
                        asyncCommitXATransaction(
                                xaTransactionID,
                                ua,
                                chainPaths,
                                commitResponse -> {
                                    if (commitResponse.getStatus() != 0
                                            && !commitResponse.getChainErrorMessages().isEmpty()) {
                                        for (XAResponse.ChainErrorMessage errorMessage :
                                                commitResponse.getChainErrorMessages()) {
                                            xaResponse.addChainErrorMessage(errorMessage);
                                        }
                                    }
                                    callback.onResponse(xaResponse);
                                });
                    } else {
                        callback.onResponse(xaResponse);
                    }
                } else {
                    callback.onResponse(new XAResponse());
                }
            }
        };
    }

    public void asyncStartXATransaction(
            String xaTransactionID,
            UniversalAccount ua,
            Set<String> resources,
            XAReduceCallback callback)
            throws WeCrossException {

        logger.info("startXATransaction, resources: {}", resources);

        Map<String, Set<String>> paths;
        try {
            paths = getChainPaths(resources);
        } catch (Exception e) {
            throw new WeCrossException(WeCrossException.ErrorCode.POST_DATA_ERROR, e.getMessage());
        }

        Set<String> chains = Collections.synchronizedSet(new HashSet<>(paths.keySet()));
        XAReduceCallback xaReduceCallback =
                getStartXAReduceCallback(paths.size(), xaTransactionID, ua, chains, callback);

        for (Map.Entry<String, Set<String>> entry : paths.entrySet()) {
            // send prepare transaction
            TransactionRequest transactionRequest = new TransactionRequest();
            transactionRequest.setMethod("startXATransaction");
            String[] args = new String[3];

            args[0] = xaTransactionID;
            String[] selfPaths = new String[entry.getValue().size()];
            String[] otherPaths = new String[resources.size() - entry.getValue().size()];

            int i = 0;
            for (String path : entry.getValue()) {
                selfPaths[i] = path;
                ++i;
            }

            i = 0;
            for (String path : resources) {
                if (!entry.getValue().contains(path)) {
                    otherPaths[i] = path;
                    ++i;
                }
            }

            XAResponse xaResponse = new XAResponse();
            try {
                args[1] = objectMapper.writeValueAsString(selfPaths);
                args[2] = objectMapper.writeValueAsString(otherPaths);
            } catch (Exception e) {
                xaResponse.addChainErrorMessage(
                        new XAResponse.ChainErrorMessage(
                                entry.getKey(),
                                "Failed to format path lists, and other succeeded chains will be committed."));
                xaReduceCallback.onResponse(xaResponse);
                continue;
            }

            transactionRequest.setArgs(args);
            transactionRequest.getOptions().put(Resource.RAW_TRANSACTION, true);

            Path proxyPath;
            try {
                proxyPath = Path.decode(entry.getValue().iterator().next());
            } catch (Exception e) {
                xaResponse.addChainErrorMessage(
                        new XAResponse.ChainErrorMessage(
                                entry.getKey(),
                                "Failed to decode path, and other succeeded chains will be committed."));
                xaReduceCallback.onResponse(xaResponse);
                continue;
            }

            proxyPath.setResource(StubConstant.PROXY_NAME);
            Resource resource = zoneManager.fetchResource(proxyPath);

            resource.asyncSendTransaction(
                    transactionRequest,
                    ua,
                    (exception, response) -> {
                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                    "startXATransaction, response: {}, exception: ",
                                    response,
                                    exception);
                        }

                        if (exception != null && !exception.isSuccess()) {
                            logger.error("startXATransaction failed, ", exception);
                            xaResponse.addChainErrorMessage(
                                    new XAResponse.ChainErrorMessage(
                                            entry.getKey(),
                                            "Failed: "
                                                    + exception.getMessage()
                                                    + ", and other succeeded chains will be committed."));
                            xaReduceCallback.onResponse(xaResponse);
                            return;
                        }

                        if (response.getErrorCode() != 0) {
                            logger.error(
                                    "startXATransaction failed: {} {}",
                                    response.getErrorCode(),
                                    response.getMessage());

                            xaResponse.addChainErrorMessage(
                                    new XAResponse.ChainErrorMessage(
                                            entry.getKey(),
                                            "Failed: "
                                                    + response.getMessage()
                                                    + ", and other succeeded chains will be committed."));
                            xaReduceCallback.onResponse(xaResponse);
                            return;
                        }

                        String result = response.getResult()[0];
                        if (!SUCCESS.equalsIgnoreCase(result.trim())) {
                            logger.error("startXATransaction failed: {}", result);
                            xaResponse.addChainErrorMessage(
                                    new XAResponse.ChainErrorMessage(entry.getKey(), result));
                        }

                        xaReduceCallback.onResponse(xaResponse);
                    });
        }
    }

    public void asyncCommitXATransaction(
            String xaTransactionID,
            UniversalAccount ua,
            Set<Path> chains,
            XAReduceCallback callback) {
        logger.info("commitXATransaction, chains: {}", chains);
        XAReduceCallback xaReduceCallback = getXAReduceCallback(chains.size(), callback);

        for (Path chainPath : chains) {
            // send prepare transaction
            TransactionRequest transactionRequest = new TransactionRequest();
            transactionRequest.setMethod("commitXATransaction");
            String[] args = new String[] {xaTransactionID};
            transactionRequest.setArgs(args);
            transactionRequest.getOptions().put(Resource.RAW_TRANSACTION, true);

            Path proxyPath = new Path(chainPath);
            proxyPath.setResource(StubConstant.PROXY_NAME);
            Resource resource = zoneManager.fetchResource(proxyPath);
            resource.asyncSendTransaction(
                    transactionRequest,
                    ua,
                    (exception, response) -> {
                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                    "commitXATransaction, response: {}, exception: ",
                                    response,
                                    exception);
                        }

                        XAResponse xaResponse = new XAResponse();

                        if (exception != null && !exception.isSuccess()) {
                            logger.error("commitXATransaction failed, ", exception);

                            xaResponse.addChainErrorMessage(
                                    new XAResponse.ChainErrorMessage(
                                            chainPath.toString(),
                                            "Commit failed: " + exception.getMessage()));
                            xaReduceCallback.onResponse(xaResponse);
                            return;
                        }

                        if (response.getErrorCode() != 0) {
                            logger.error(
                                    "commitXATransaction failed: {} {}",
                                    response.getErrorCode(),
                                    response.getMessage());

                            xaResponse.addChainErrorMessage(
                                    new XAResponse.ChainErrorMessage(
                                            chainPath.toString(),
                                            "Commit failed: " + response.getMessage()));
                            xaReduceCallback.onResponse(xaResponse);
                            return;
                        }

                        String result = response.getResult()[0];
                        if (!SUCCESS.equalsIgnoreCase(result.trim())) {
                            logger.error("commitXATransaction failed: {}", result);
                            xaResponse.addChainErrorMessage(
                                    new XAResponse.ChainErrorMessage(chainPath.toString(), result));
                        }

                        xaReduceCallback.onResponse(xaResponse);
                    });
        }
    }

    public void asyncRollbackXATransaction(
            String xaTransactionID,
            UniversalAccount ua,
            Set<Path> chains,
            XAReduceCallback callback) {

        XAReduceCallback xaReduceCallback = getXAReduceCallback(chains.size(), callback);

        for (Path chainPath : chains) {
            // send rollback transaction
            TransactionRequest transactionRequest = new TransactionRequest();
            transactionRequest.setMethod("rollbackXATransaction");
            String[] args = new String[] {xaTransactionID};
            transactionRequest.setArgs(args);
            transactionRequest.getOptions().put(Resource.RAW_TRANSACTION, true);

            Path proxyPath = new Path(chainPath);
            proxyPath.setResource(StubConstant.PROXY_NAME);
            Resource resource = zoneManager.fetchResource(proxyPath);

            XAResponse xaResponse = new XAResponse();
            resource.asyncSendTransaction(
                    transactionRequest,
                    ua,
                    (exception, response) -> {
                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                    "rollbackXATransaction, response: {}, exception: ",
                                    response,
                                    exception);
                        }

                        if (exception != null && !exception.isSuccess()) {
                            logger.error("rollbackXATransaction failed, ", exception);

                            xaResponse.addChainErrorMessage(
                                    new XAResponse.ChainErrorMessage(
                                            chainPath.toString(),
                                            "Rollback failed: " + exception.getMessage()));
                            xaReduceCallback.onResponse(xaResponse);
                            return;
                        }

                        if (response.getErrorCode() != 0) {
                            logger.error(
                                    "rollbackXATransaction failed: {} {}",
                                    response.getErrorCode(),
                                    response.getMessage());

                            xaResponse.addChainErrorMessage(
                                    new XAResponse.ChainErrorMessage(
                                            chainPath.toString(),
                                            "Rollback failed: " + response.getMessage()));
                            xaReduceCallback.onResponse(xaResponse);
                            return;
                        }

                        String result = response.getResult()[0];
                        if (!SUCCESS.equalsIgnoreCase(result.trim())) {
                            logger.error("rollbackXATransaction failed: {}", result);
                            xaResponse.addChainErrorMessage(
                                    new XAResponse.ChainErrorMessage(chainPath.toString(), result));
                        }

                        xaReduceCallback.onResponse(xaResponse);
                    });
        }
    }

    public interface GetXATransactionCallback {
        void onResponse(XATransactionResponse xaTransactionResponse);
    }

    private GetXATransactionCallback getXATransactionReduceCallback(
            int size, GetXATransactionCallback callback) {
        AtomicInteger finished = new AtomicInteger();
        List<XATransaction> xaTransactions = Collections.synchronizedList(new LinkedList<>());
        List<XAResponse.ChainErrorMessage> errors =
                Collections.synchronizedList(new LinkedList<>());

        return (xaTransactionResponse) -> {
            if (!xaTransactionResponse.getXaResponse().getChainErrorMessages().isEmpty()) {
                errors.add(xaTransactionResponse.getXaResponse().getChainErrorMessages().get(0));
            } else {
                xaTransactions.add(xaTransactionResponse.getXaTransaction());
            }

            int current = finished.addAndGet(1);
            if (current == size) {
                XATransactionResponse response = new XATransactionResponse();
                XAResponse xaResponse = new XAResponse();

                if (!errors.isEmpty()) {
                    xaResponse.setStatus(-1);
                    xaResponse.setChainErrorMessages(errors);
                    response.setXaResponse(xaResponse);
                }

                if (xaTransactions.isEmpty()) {
                    callback.onResponse(response);
                } else {
                    if (xaTransactions.size() < size) {
                        logger.warn("getXATransaction missing some steps");
                    }

                    // merge and sort xa transaction steps
                    List<XATransactionStep> allSteps = new ArrayList<>();
                    for (XATransaction xaTransaction : xaTransactions) {
                        allSteps.addAll(xaTransaction.getXaTransactionSteps());
                    }

                    allSteps.sort(Comparator.comparingLong(XATransactionStep::getXaTransactionSeq));

                    XATransaction finalXATransaction = xaTransactions.get(0);
                    finalXATransaction.setXaTransactionSteps(allSteps);

                    // recover username
                    finalXATransaction.recoverUsername(accountManager);
                    response.setXaTransaction(finalXATransaction);

                    callback.onResponse(response);
                }
            }
        };
    }

    public void asyncGetXATransaction(
            String xaTransactionID,
            UniversalAccount ua,
            Set<Path> chains,
            GetXATransactionCallback callback) {

        GetXATransactionCallback reduceCallback =
                getXATransactionReduceCallback(chains.size(), callback);

        for (Path chainPath : chains) {
            TransactionRequest transactionRequest = new TransactionRequest();
            transactionRequest.setMethod("getXATransaction");
            String[] args = new String[] {xaTransactionID};
            transactionRequest.setArgs(args);
            transactionRequest.getOptions().put(Resource.RAW_TRANSACTION, true);

            Path proxyPath = new Path(chainPath);
            proxyPath.setResource(StubConstant.PROXY_NAME);
            Resource resource = zoneManager.fetchResource(proxyPath);

            resource.asyncCall(
                    transactionRequest,
                    ua,
                    (exception, response) -> {
                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                    "getXATransaction, response: {}, exception: ",
                                    response,
                                    exception);
                        }

                        XATransactionResponse xaTransactionResponse = new XATransactionResponse();

                        if (exception != null && !exception.isSuccess()) {
                            logger.error("getXATransaction error: ", exception);
                            xaTransactionResponse
                                    .getXaResponse()
                                    .addChainErrorMessage(
                                            new XAResponse.ChainErrorMessage(
                                                    chainPath.toString(), exception.getMessage()));
                            reduceCallback.onResponse(xaTransactionResponse);
                            return;
                        }

                        if (response.getErrorCode() != 0) {
                            logger.error(
                                    "getXATransaction failed: {} {}",
                                    response.getErrorCode(),
                                    response.getMessage());

                            xaTransactionResponse
                                    .getXaResponse()
                                    .addChainErrorMessage(
                                            new XAResponse.ChainErrorMessage(
                                                    chainPath.toString(), response.getMessage()));
                            reduceCallback.onResponse(xaTransactionResponse);
                            return;
                        }

                        // decode return value
                        try {
                            // notice: must remove unseen char
                            String jsonStr =
                                    response.getResult()[0].replace(
                                            new String(Character.toChars(0)), "");

                            if (logger.isDebugEnabled()) {
                                logger.debug("XATransaction: {}", jsonStr);
                            }

                            XATransaction xaTransaction =
                                    objectMapper.readValue(jsonStr, XATransaction.class);

                            xaTransactionResponse.setXaTransaction(xaTransaction);
                            reduceCallback.onResponse(xaTransactionResponse);
                        } catch (Exception e) {
                            logger.error("Decode XATransaction json error: ", e);
                            xaTransactionResponse
                                    .getXaResponse()
                                    .addChainErrorMessage(
                                            new XAResponse.ChainErrorMessage(
                                                    chainPath.toString(),
                                                    "Decode XATransaction json error"));
                            reduceCallback.onResponse(xaTransactionResponse);
                        }
                    });
        }
    }

    public interface ListXATransactionsCallback {
        void onResponse(WeCrossException e, XATransactionListResponse xaTransactionListResponse);
    }

    public interface ListXAReduceCallback {
        void onResponse(String chain, ListXAResponse listXAResponse);
    }

    private ListXAReduceCallback getListXAReduceCallback(
            int count, Map<String, Long> offsets, int size, ListXATransactionsCallback callback) {
        AtomicInteger finished = new AtomicInteger();
        Map<String, List<XATransactionListResponse.XA>> xaResponses =
                Collections.synchronizedMap(new HashMap<>());
        List<XAResponse.ChainErrorMessage> errors =
                Collections.synchronizedList(new LinkedList<>());
        Map<String, Long> nextOffsets = new ConcurrentHashMap<>(offsets);

        return (chain, listXAResponse) -> {
            if (Objects.nonNull(listXAResponse.getChainErrorMessage())) {
                errors.add(listXAResponse.getChainErrorMessage());
            } else {
                xaResponses.put(chain, listXAResponse.getXaTransactions());
            }

            if (nextOffsets.get(chain) == -1) {
                nextOffsets.put(chain, listXAResponse.getTotal() - 1);
            }

            int current = finished.addAndGet(1);

            // all finished
            if (current == count) {
                XATransactionListResponse response = new XATransactionListResponse();
                XAResponse xaResponse = new XAResponse();

                if (!errors.isEmpty()) {
                    xaResponse.setStatus(-1);
                    xaResponse.setChainErrorMessages(errors);
                    response.setXaResponse(xaResponse);
                }

                int num = 0;
                int total = 0;
                Set<String> xaTransactionIDs = new HashSet<>();
                Set<String> finishedChains = new HashSet<>();

                // init index for each chain
                Map<String, Integer> indexs = new HashMap<>();
                for (String key : xaResponses.keySet()) {
                    indexs.put(key, 0);
                }

                while (num < xaResponses.size() && total < size) {
                    long maxTimestamp = 0;
                    String maxTimestampChain = null;

                    for (String currentChain : xaResponses.keySet()) {
                        if (finishedChains.contains(currentChain)) {
                            continue;
                        }

                        if (indexs.get(currentChain) == xaResponses.get(currentChain).size()) {
                            // current chain finished, update offset and index
                            Long nextOffset =
                                    indexs.get(currentChain) == size
                                            ? offsets.get(currentChain) - size
                                            : -1;
                            nextOffsets.put(currentChain, nextOffset);
                            finishedChains.add(currentChain);
                            num++;
                        } else {
                            int currentIndex = indexs.get(currentChain);
                            String currentXATransactionID =
                                    xaResponses
                                            .get(currentChain)
                                            .get(currentIndex)
                                            .getXaTransactionID();

                            if (xaTransactionIDs.contains(currentXATransactionID)) {
                                // current xa transactionID existed
                                indexs.put(currentChain, currentIndex + 1);
                                Long nextOffset = nextOffsets.get(currentChain) - 1;
                                nextOffsets.put(currentChain, nextOffset);
                            } else {
                                // check current timestamp
                                long currentTimestamp =
                                        xaResponses
                                                .get(currentChain)
                                                .get(currentIndex)
                                                .getTimestamp();
                                if (currentTimestamp > maxTimestamp) {
                                    maxTimestamp = currentTimestamp;
                                    maxTimestampChain = currentChain;
                                }
                            }
                        }

                        if (num == xaResponses.size()) {
                            break;
                        }
                    }

                    // find one with max timestamp
                    if (Objects.nonNull(maxTimestampChain)) {
                        int maxOneIndex = indexs.get(maxTimestampChain);
                        String maxOneXATransactionID =
                                xaResponses
                                        .get(maxTimestampChain)
                                        .get(maxOneIndex)
                                        .getXaTransactionID();

                        // add new xa
                        xaTransactionIDs.add(maxOneXATransactionID);
                        response.addXATransactionInfo(
                                xaResponses.get(maxTimestampChain).get(maxOneIndex));
                        total++;

                        // update offset and index
                        indexs.put(maxTimestampChain, maxOneIndex + 1);
                        Long nextOffset = nextOffsets.get(maxTimestampChain) - 1;
                        nextOffsets.put(maxTimestampChain, nextOffset);
                    }

                    if (num == xaResponses.size() || total == size) {
                        break;
                    }
                }

                response.setFinished(num == xaResponses.size() || total < size);

                // check offsets
                int finishedChain = 0;
                for (String key : nextOffsets.keySet()) {
                    if (nextOffsets.get(key) == -1) {
                        finishedChain++;
                    }
                }
                if (finishedChain == nextOffsets.size()) {
                    response.setFinished(true);
                }

                response.setNextOffsets(nextOffsets);
                response.recoverUsername(accountManager);
                callback.onResponse(null, response);
            }
        };
    }

    public void asyncListXATransactions(
            UniversalAccount ua,
            Map<String, Long> offsets,
            int size,
            ListXATransactionsCallback callback) {

        try {
            if (size <= 0 || size > WeCrossDefault.MAX_SIZE_FOR_LIST) {
                callback.onResponse(
                        new WeCrossException(
                                WeCrossException.ErrorCode.POST_DATA_ERROR,
                                "Wrong size, 1 <= size <= " + WeCrossDefault.MAX_SIZE_FOR_LIST),
                        null);
                return;
            }

            XATransactionListResponse response = new XATransactionListResponse();

            List<Path> chainPaths = setToList(zoneManager.getAllChainsInfo(false).keySet());

            int chainNum = chainPaths.size();
            if (chainNum == 0) {
                // no chain found
                response.setFinished(true);
                callback.onResponse(null, response);
                return;
            }

            boolean requireIgnore = true;
            if (offsets.isEmpty()) {
                // first time to list, reset offsets
                requireIgnore = false;
                for (Path chain : chainPaths) {
                    offsets.put(chain.toString(), -1L);
                }
            }

            ListXAReduceCallback reduceCallback =
                    getListXAReduceCallback(offsets.size(), offsets, size, callback);
            for (String chain : offsets.keySet()) {
                if (!requireIgnore || offsets.get(chain) != -1L) {
                    asyncListXATransactions(
                            ua, Path.decode(chain), offsets.get(chain), size, reduceCallback);
                } else {
                    // not first time to list, -1 means finished
                    reduceCallback.onResponse(chain, new ListXAResponse());
                }
            }

        } catch (WeCrossException e) {
            logger.warn("listXATransactions, error: ", e);
            callback.onResponse(e, null);
        } catch (Exception e) {
            logger.warn("listXATransactions, error: ", e);
            callback.onResponse(
                    new WeCrossException(
                            WeCrossException.ErrorCode.INTERNAL_ERROR, "Internal error"),
                    null);
        }
    }

    private void asyncListXATransactions(
            UniversalAccount ua, Path path, Long offset, int size, ListXAReduceCallback callback) {
        if (logger.isDebugEnabled()) {
            logger.debug("listXATransactions, path: {}, offset: {}, size: {}", path, offset, size);
        }

        try {
            TransactionRequest transactionRequest = new TransactionRequest();
            transactionRequest.getOptions().put(Resource.RAW_TRANSACTION, true);
            transactionRequest.setMethod("listXATransactions");
            transactionRequest.setArgs(new String[] {String.valueOf(offset), String.valueOf(size)});

            Path proxyPath = new Path(path);
            proxyPath.setResource(StubConstant.PROXY_NAME);
            Resource resource = zoneManager.fetchResource(proxyPath);

            resource.asyncCall(
                    transactionRequest,
                    ua,
                    (transactionException, transactionResponse) -> {
                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                    "listXATransactions, response: {}, exception: ",
                                    transactionResponse,
                                    transactionException);
                        }

                        ListXAResponse response = new ListXAResponse();
                        XAResponse.ChainErrorMessage chainErrorMessage = null;

                        if (Objects.nonNull(transactionException)
                                && !transactionException.isSuccess()) {
                            logger.warn(
                                    "Failed to list xa transaction: {}",
                                    transactionException.getMessage());
                            chainErrorMessage =
                                    new XAResponse.ChainErrorMessage(
                                            path.toString(), transactionException.getMessage());
                        } else if (transactionResponse.getErrorCode() != 0) {
                            logger.warn(
                                    "Failed to list xa transaction: {}",
                                    transactionResponse.getMessage());
                            chainErrorMessage =
                                    new XAResponse.ChainErrorMessage(
                                            path.toString(), transactionResponse.getMessage());
                        } else {
                            String result = transactionResponse.getResult()[0];
                            if (Objects.nonNull(result)
                                    && !"{\"total\":0,\"xaTransactions\":[]}".equals(result)) {
                                try {
                                    XATransactionListResponse.XAList xaList =
                                            objectMapper.readValue(
                                                    result.replace(
                                                            new String(Character.toChars(0)), ""),
                                                    XATransactionListResponse.XAList.class);
                                    response.setXaTransactions(
                                            Arrays.asList(xaList.getXaTransactions()));
                                    response.setTotal(xaList.getTotal());
                                } catch (Exception e) {
                                    logger.warn("Failed to decode json: {}", result);
                                }
                            }
                        }

                        response.setChainErrorMessage(chainErrorMessage);
                        callback.onResponse(path.toString(), response);
                    });
        } catch (Exception e) {
            logger.warn("Failed to list xa transaction: ", e);
            ListXAResponse response = new ListXAResponse();
            XAResponse.ChainErrorMessage chainErrorMessage =
                    new XAResponse.ChainErrorMessage(path.toString(), "Internal error");
            response.setChainErrorMessage(chainErrorMessage);
            callback.onResponse(path.toString(), response);
        }
    }

    private static class ListXAResponse {
        private XAResponse.ChainErrorMessage chainErrorMessage;

        private Long total = new Long(0);

        private List<XATransactionListResponse.XA> xaTransactions = new ArrayList<>();

        public XAResponse.ChainErrorMessage getChainErrorMessage() {
            return chainErrorMessage;
        }

        public void setChainErrorMessage(XAResponse.ChainErrorMessage chainErrorMessage) {
            this.chainErrorMessage = chainErrorMessage;
        }

        public Long getTotal() {
            return total;
        }

        public void setTotal(Long total) {
            this.total = total;
        }

        public List<XATransactionListResponse.XA> getXaTransactions() {
            return xaTransactions;
        }

        public void setXaTransactions(List<XATransactionListResponse.XA> xaTransactions) {
            this.xaTransactions = xaTransactions;
        }

        @Override
        public String toString() {
            return "ListXAResponse{"
                    + "chainErrorMessage="
                    + chainErrorMessage
                    + ", total="
                    + total
                    + ", xaTransactions="
                    + xaTransactions
                    + '}';
        }
    }

    public void registerTask(TaskManager taskManager) {}

    public ZoneManager getZoneManager() {
        return zoneManager;
    }

    public void setZoneManager(ZoneManager zoneManager) {
        this.zoneManager = zoneManager;
    }

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public void setAccountManager(AccountManager accountManager) {
        this.accountManager = accountManager;
    }
}
