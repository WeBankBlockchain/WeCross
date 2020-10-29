package com.webank.wecross.routine.xa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.account.AccountManager;
import com.webank.wecross.account.UniversalAccount;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.polling.TaskManager;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.*;
import com.webank.wecross.zone.ZoneManager;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
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

    private ReduceCallback getReduceCallback(int size, ReduceCallback callback) {
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

        logger.info("StartXATransaction, resources: {}", resources);

        Map<String, Set<Path>> paths = getChainPaths(resources);

        ReduceCallback reduceCallback = getReduceCallback(paths.size(), callback);

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
                        if (exception != null && !exception.isSuccess()) {
                            logger.error("StartXATransaction failed, ", exception);
                            xaResponse.addChainErrorMessage(
                                    new XAResponse.ChainErrorMessage(
                                            entry.getKey(), exception.getMessage()));
                            reduceCallback.onResponse(xaResponse);
                            return;
                        }

                        if (response.getErrorCode() != 0) {
                            logger.error(
                                    "StartXATransaction failed: {} {}",
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
                            logger.error("StartXATransaction failed: {}", result);
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
        logger.info("CommitXATransaction, chains: {}", chains);

        ReduceCallback reduceCallback = getReduceCallback(chains.size(), callback);

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
                        if (exception != null && !exception.isSuccess()) {
                            logger.error("CommitXATransaction failed, ", exception);

                            xaResponse.addChainErrorMessage(
                                    new XAResponse.ChainErrorMessage(
                                            chainPath.toString(), exception.getMessage()));
                            reduceCallback.onResponse(xaResponse);
                            return;
                        }

                        if (response.getErrorCode() != 0) {
                            logger.error(
                                    "CommitXATransaction failed: {} {}",
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
                            logger.error("CommitXATransaction failed: {}", result);
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

        ReduceCallback reduceCallback = getReduceCallback(chains.size(), callback);

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
                        if (exception != null && !exception.isSuccess()) {
                            logger.error("RollbackXATransaction failed, ", exception);

                            xaResponse.addChainErrorMessage(
                                    new XAResponse.ChainErrorMessage(
                                            chainPath.toString(), exception.getMessage()));
                            reduceCallback.onResponse(xaResponse);
                            return;
                        }

                        if (response.getErrorCode() != 0) {
                            logger.error(
                                    "RollbackXATransaction failed: {} {}",
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
                            logger.error("RollbackXATransaction failed: {}", result);
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
                        logger.warn("GetXATransaction missing some steps");
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
                        XATransactionResponse xaTransactionResponse = new XATransactionResponse();

                        if (exception != null && !exception.isSuccess()) {
                            logger.error("GetXATransaction error: ", exception);
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
                                    "GetXATransaction failed: {} {}",
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

    public interface ListXATransactionCallback {
        void onResponse(WeCrossException e, XATransactionListResponse xaTransactionListResponse);
    }

    public void asyncGetTransactionIDs(
            UniversalAccount ua,
            int size,
            List<Integer> offsets,
            ListXATransactionCallback callback) {

        try {
            if (size <= 0) {
                callback.onResponse(
                        new WeCrossException(
                                WeCrossException.ErrorCode.POST_DATA_ERROR,
                                "Size must be positive"),
                        null);
                return;
            }

            XATransactionListResponse response = new XATransactionListResponse();

            List<Path> chainPaths = setToList(zoneManager.getAllChainsInfo(false).keySet());
            chainPaths.sort(Comparator.comparing(Path::toString));

            int chainNum = chainPaths.size();
            if (chainNum == 0) {
                // no chain found
                response.setFinished(true);
                callback.onResponse(null, response);
                return;
            }

            // reset abnormal offsets
            if (offsets.isEmpty() || offsets.size() < chainNum) {
                offsets.clear();
                for (int i = 0; i < chainNum; i++) {
                    offsets.add(0);
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Offsets: {}", Arrays.toString(offsets.toArray()));
            }

            // prepare task
            Set<String> xaTransactionIDs = new HashSet<>();

            // number of unfinished chain
            int num = chainNum;
            for (int i = 0; i < chainNum; i++) {
                if (offsets.get(i) == -1) {
                    num--;
                }
            }

            if (num == 0) {
                response.setFinished(true);
                callback.onResponse(null, response);
                return;
            }

            int count = 0;
            while (num != 0 && count < size) {
                for (int i = 0; i < chainNum; i++) {
                    if (offsets.get(i) != -1) {
                        // size for each chain will be updated after query chain
                        int perSize = (size - count) / num;
                        perSize = perSize == 0 ? 1 : perSize;

                        ListXAResponse perResponse =
                                listXATransactions(ua, chainPaths.get(i), offsets.get(i), perSize);

                        offsets.set(i, perResponse.getNextOffset());
                        if (perResponse.getNextOffset() == -1) {
                            num--;
                        }

                        for (XATransactionListResponse.XATransactionInfo info :
                                perResponse.getInfoList()) {
                            if (!xaTransactionIDs.contains(info.getXaTransactionID())) {
                                xaTransactionIDs.add(info.getXaTransactionID());
                                // add xa transaction info
                                response.addXATransactionInfo(info);
                                count++;
                            }
                        }

                        if (num == 0 || count == size) {
                            break;
                        }
                    }
                }
            }

            if (response.getInfoList().size() < size) {
                response.setFinished(true);
            }
            response.setOffsets(offsets);
            response.recoverUsername(accountManager);

            callback.onResponse(null, response);
        } catch (WeCrossException e) {
            callback.onResponse(e, null);
        } catch (Exception e) {
            callback.onResponse(
                    new WeCrossException(
                            WeCrossException.ErrorCode.INTERNAL_ERROR, "Internal error"),
                    null);
        }
    }

    private ListXAResponse listXATransactions(UniversalAccount ua, Path path, int offset, int size)
            throws WeCrossException {
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.getOptions().put(Resource.RAW_TRANSACTION, true);
        transactionRequest.setMethod("listXATransactions");
        transactionRequest.setArgs(new String[] {String.valueOf(offset), String.valueOf(size)});

        Path proxyPath = new Path(path);
        proxyPath.setResource(StubConstant.PROXY_NAME);
        Resource resource = zoneManager.fetchResource(proxyPath);

        CompletableFuture<String> future = new CompletableFuture<>();
        resource.asyncCall(
                transactionRequest,
                ua,
                (transactionException, transactionResponse) -> {
                    if (Objects.nonNull(transactionException)
                            && !transactionException.isSuccess()) {
                        logger.warn(
                                "Failed to list xa transaction: {}",
                                transactionException.getMessage());
                        if (!future.isCancelled()) {
                            future.complete(null);
                        }
                    } else if (transactionResponse.getErrorCode() != 0) {
                        logger.warn(
                                "Failed to list xa transaction: {}",
                                transactionResponse.getMessage());
                        if (!future.isCancelled()) {
                            future.complete(null);
                        }
                    } else {
                        if (!future.isCancelled()) {
                            future.complete(transactionResponse.getResult()[0]);
                        }
                    }
                });

        String xaList;
        try {
            xaList = future.get(10000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            future.cancel(true);
            logger.warn("Failed to list xa transaction: ", e);
            throw new WeCrossException(WeCrossException.ErrorCode.INTERNAL_ERROR, "Internal error");
        }

        if (Objects.isNull(xaList)) {
            throw new WeCrossException(
                    WeCrossException.ErrorCode.CALL_CONTRACT_ERROR,
                    "Failed to list xa transaction from " + path.toString());
        }

        ListXAResponse response = new ListXAResponse();
        if ("[]".equals(xaList.trim())) {
            return response;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("chain: {}, xa transaction list: {}", path, xaList);
        }

        XATransactionListResponse.XATransactionInfo[] xaInfos;

        try {
            xaInfos =
                    objectMapper.readValue(
                            xaList.replace(new String(Character.toChars(0)), ""),
                            XATransactionListResponse.XATransactionInfo[].class);
        } catch (Exception e) {
            throw new WeCrossException(
                    WeCrossException.ErrorCode.INTERNAL_ERROR, "Failed to decode json: " + xaList);
        }

        response.setInfoList(Arrays.asList(xaInfos));
        if (response.getInfoList().size() == size) {
            response.setNextOffset(offset + response.getInfoList().size());
        }

        return response;
    }

    private static class ListXAResponse {
        private List<XATransactionListResponse.XATransactionInfo> infoList = new ArrayList<>();
        private int nextOffset = -1;

        public List<XATransactionListResponse.XATransactionInfo> getInfoList() {
            return infoList;
        }

        public void setInfoList(List<XATransactionListResponse.XATransactionInfo> infoList) {
            this.infoList = infoList;
        }

        public int getNextOffset() {
            return nextOffset;
        }

        public void setNextOffset(int nextOffset) {
            this.nextOffset = nextOffset;
        }

        @Override
        public String toString() {
            return "ListXAResponse{" + "infoList=" + infoList + ", nextOffset=" + nextOffset + '}';
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
