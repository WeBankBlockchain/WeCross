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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XATransactionManager {
    private Logger logger = LoggerFactory.getLogger(XATransactionManager.class);
    ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    private ZoneManager zoneManager;
    private AccountManager accountManager;

    private static final String SUCCESS = "success";

    public interface ReduceCallback {
        void onResponse(XAResponse xaResponse);
    }

    public Map<String, Set<Path>> getChainPaths(Set<Path> resources) {
        Map<String, Set<Path>> zone2Path = new HashMap<String, Set<Path>>();
        for (Path path : resources) {
            String key = path.getZone() + "." + path.getChain();
            zone2Path.computeIfAbsent(key, k -> new HashSet<>());
            zone2Path.get(key).add(path);
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

    private ReduceCallback getListXAReduceCallback(int size, ReduceCallback callback) {
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

    public void asyncStartXATransaction(
            String xaTransactionID,
            UniversalAccount ua,
            Set<Path> resources,
            ReduceCallback callback) {

        logger.info("startXATransaction, resources: {}", resources);

        Map<String, Set<Path>> paths = getChainPaths(resources);

        ReduceCallback reduceCallback = getListXAReduceCallback(paths.size(), callback);

        for (Map.Entry<String, Set<Path>> entry : paths.entrySet()) {
            Path tempPath = entry.getValue().iterator().next();
            // send prepare transaction
            TransactionRequest transactionRequest = new TransactionRequest();
            transactionRequest.setMethod("startXATransaction");
            String[] args = new String[entry.getValue().size() + resources.size() + 2];

            args[0] = xaTransactionID;
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

            Path proxyPath = new Path(tempPath);
            proxyPath.setResource(StubConstant.PROXY_NAME);
            Resource resource = zoneManager.fetchResource(proxyPath);

            XAResponse xaResponse = new XAResponse();
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
                                            entry.getKey(), exception.getMessage()));
                            reduceCallback.onResponse(xaResponse);
                            return;
                        }

                        if (response.getErrorCode() != 0) {
                            logger.error(
                                    "startXATransaction failed: {} {}",
                                    response.getErrorCode(),
                                    response.getMessage());

                            xaResponse.addChainErrorMessage(
                                    new XAResponse.ChainErrorMessage(
                                            entry.getKey(), response.getMessage()));
                            reduceCallback.onResponse(xaResponse);
                            return;
                        }

                        String result = response.getResult()[0];
                        if (!SUCCESS.equalsIgnoreCase(result.trim())) {
                            logger.error("startXATransaction failed: {}", result);
                            xaResponse.addChainErrorMessage(
                                    new XAResponse.ChainErrorMessage(entry.getKey(), result));
                        }

                        reduceCallback.onResponse(xaResponse);
                    });
        }
    }

    public void asyncCommitXATransaction(
            String xaTransactionID,
            UniversalAccount ua,
            Set<Path> chains,
            ReduceCallback callback) {
        logger.info("commitXATransaction, chains: {}", chains);

        ReduceCallback reduceCallback = getListXAReduceCallback(chains.size(), callback);

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

            XAResponse xaResponse = new XAResponse();
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

                        if (exception != null && !exception.isSuccess()) {
                            logger.error("commitXATransaction failed, ", exception);

                            xaResponse.addChainErrorMessage(
                                    new XAResponse.ChainErrorMessage(
                                            chainPath.toString(), exception.getMessage()));
                            reduceCallback.onResponse(xaResponse);
                            return;
                        }

                        if (response.getErrorCode() != 0) {
                            logger.error(
                                    "commitXATransaction failed: {} {}",
                                    response.getErrorCode(),
                                    response.getMessage());

                            xaResponse.addChainErrorMessage(
                                    new XAResponse.ChainErrorMessage(
                                            chainPath.toString(), response.getMessage()));
                            reduceCallback.onResponse(xaResponse);
                            return;
                        }

                        String result = response.getResult()[0];
                        if (!SUCCESS.equalsIgnoreCase(result.trim())) {
                            logger.error("commitXATransaction failed: {}", result);
                            xaResponse.addChainErrorMessage(
                                    new XAResponse.ChainErrorMessage(chainPath.toString(), result));
                        }

                        reduceCallback.onResponse(xaResponse);
                    });
        }
    }

    public void asyncRollbackXATransaction(
            String xaTransactionID,
            UniversalAccount ua,
            Set<Path> chains,
            ReduceCallback callback) {

        ReduceCallback reduceCallback = getListXAReduceCallback(chains.size(), callback);

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
                                            chainPath.toString(), exception.getMessage()));
                            reduceCallback.onResponse(xaResponse);
                            return;
                        }

                        if (response.getErrorCode() != 0) {
                            logger.error(
                                    "rollbackXATransaction failed: {} {}",
                                    response.getErrorCode(),
                                    response.getMessage());

                            xaResponse.addChainErrorMessage(
                                    new XAResponse.ChainErrorMessage(
                                            chainPath.toString(), response.getMessage()));
                            reduceCallback.onResponse(xaResponse);
                            return;
                        }

                        String result = response.getResult()[0];
                        if (!SUCCESS.equalsIgnoreCase(result.trim())) {
                            logger.error("rollbackXATransaction failed: {}", result);
                            xaResponse.addChainErrorMessage(
                                    new XAResponse.ChainErrorMessage(chainPath.toString(), result));
                        }

                        reduceCallback.onResponse(xaResponse);
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
            int count,
            Map<String, Integer> offsets,
            int size,
            ListXATransactionsCallback callback) {
        AtomicInteger finished = new AtomicInteger();
        Map<String, List<XATransactionListResponse.XA>> xaResponses =
                Collections.synchronizedMap(new HashMap<>());
        List<XAResponse.ChainErrorMessage> errors =
                Collections.synchronizedList(new LinkedList<>());

        return (chain, listXAResponse) -> {
            if (Objects.nonNull(listXAResponse.getChainErrorMessage())) {
                errors.add(listXAResponse.getChainErrorMessage());
            } else {
                xaResponses.put(chain, listXAResponse.getXaList());
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

                // deep copy
                Map<String, Integer> nextOffsets = new HashMap<>(offsets);

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
                            int nextOffset =
                                    indexs.get(currentChain) == size
                                            ? offsets.get(currentChain) + size
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
                                int nextOffset = nextOffsets.get(currentChain) + 1;
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
                        int nextOffset = nextOffsets.get(maxTimestampChain) + 1;
                        nextOffsets.put(maxTimestampChain, nextOffset);
                    }

                    if (num == xaResponses.size() || total == size) {
                        break;
                    }
                }

                response.setFinished(num == xaResponses.size() || total < size);
                response.setNextOffsets(nextOffsets);
                response.recoverUsername(accountManager);
                callback.onResponse(null, response);
            }
        };
    }

    public void asyncListXATransactions(
            UniversalAccount ua,
            Map<String, Integer> offsets,
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

            // reset offsets
            if (offsets.isEmpty()) {
                for (Path chain : chainPaths) {
                    offsets.put(chain.toString(), 0);
                }
            }

            ListXAReduceCallback reduceCallback =
                    getListXAReduceCallback(offsets.size(), offsets, size, callback);
            for (String chain : offsets.keySet()) {
                asyncListXATransactions(
                        ua, Path.decode(chain), offsets.get(chain), size, reduceCallback);
            }

        } catch (WeCrossException e) {
            callback.onResponse(e, null);
        } catch (Exception e) {
            callback.onResponse(
                    new WeCrossException(
                            WeCrossException.ErrorCode.INTERNAL_ERROR, "Internal error"),
                    null);
        }
    }

    private void asyncListXATransactions(
            UniversalAccount ua, Path path, int offset, int size, ListXAReduceCallback callback) {
        if (logger.isDebugEnabled()) {
            logger.debug("listXATransactions, path: {}, offset: {}, size: {}", path, offset, size);
        }

        if (offset < 0) {
            callback.onResponse(path.toString(), new ListXAResponse());
            return;
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
                            if (Objects.nonNull(result) && !"[]".equals(result)) {
                                XATransactionListResponse.XA[] xas =
                                        new XATransactionListResponse.XA[0];
                                try {
                                    xas =
                                            objectMapper.readValue(
                                                    result.replace(
                                                            new String(Character.toChars(0)), ""),
                                                    XATransactionListResponse.XA[].class);
                                } catch (Exception e) {
                                    logger.warn("Failed to decode json: {}", result);
                                }
                                response.setXaList(Arrays.asList(xas));
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

        private List<XATransactionListResponse.XA> xaList = new ArrayList<>();

        public XAResponse.ChainErrorMessage getChainErrorMessage() {
            return chainErrorMessage;
        }

        public void setChainErrorMessage(XAResponse.ChainErrorMessage chainErrorMessage) {
            this.chainErrorMessage = chainErrorMessage;
        }

        public List<XATransactionListResponse.XA> getXaList() {
            return xaList;
        }

        public void setXaList(List<XATransactionListResponse.XA> xaList) {
            this.xaList = xaList;
        }

        @Override
        public String toString() {
            return "ListXAResponse{"
                    + "chainErrorMessage="
                    + chainErrorMessage
                    + ", xaList="
                    + xaList
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
