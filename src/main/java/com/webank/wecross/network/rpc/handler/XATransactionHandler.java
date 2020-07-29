package com.webank.wecross.network.rpc.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.account.AccountManager;
import com.webank.wecross.common.NetworkQueryStatus;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.restserver.RestRequest;
import com.webank.wecross.restserver.RestResponse;
import com.webank.wecross.routine.xa.XATransactionManager;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.TransactionRequest;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XATransactionHandler implements URIHandler {
    private Logger logger = LoggerFactory.getLogger(XATransactionHandler.class);
    private ObjectMapper objectMapper = new ObjectMapper();
    private AccountManager accountManager;
    private XATransactionManager xaTransactionManager;

    public static class XAStartTransactionRequest {
        private String transactionID;
        private List<String> accounts;
        private List<String> paths;

        public String getTransactionID() {
            return transactionID;
        }

        public void setTransactionID(String transactionID) {
            this.transactionID = transactionID;
        }

        public List<String> getAccounts() {
            return accounts;
        }

        public void setAccounts(List<String> accounts) {
            this.accounts = accounts;
        }

        public List<String> getPaths() {
            return paths;
        }

        public void setPaths(List<String> paths) {
            this.paths = paths;
        }
    }

    public static class XACommitTransactionRequest {
        private String transactionID;
        private List<String> paths;
        private List<String> accounts;

        public String getTransactionID() {
            return transactionID;
        }

        public void setTransactionID(String transactionID) {
            this.transactionID = transactionID;
        }

        public List<String> getPaths() {
            return paths;
        }

        public void setPaths(List<String> paths) {
            this.paths = paths;
        }

        public List<String> getAccounts() {
            return accounts;
        }

        public void setAccounts(List<String> accounts) {
            this.accounts = accounts;
        }
    }

    public static class XARollbackTransactionRequest {
        private String transactionID;
        private List<String> paths;
        private List<String> accounts;

        public String getTransactionID() {
            return transactionID;
        }

        public void setTransactionID(String transactionID) {
            this.transactionID = transactionID;
        }

        public List<String> getPaths() {
            return paths;
        }

        public void setPaths(List<String> paths) {
            this.paths = paths;
        }

        public List<String> getAccounts() {
            return accounts;
        }

        public void setAccounts(List<String> accounts) {
            this.accounts = accounts;
        }
    }

    public static class XAGetTransactionInfoRequest {
        private String transactionID;
        private List<String> paths;
        private List<String> accounts;

        public String getTransactionID() {
            return transactionID;
        }

        public void setTransactionID(String transactionID) {
            this.transactionID = transactionID;
        }

        public List<String> getPaths() {
            return paths;
        }

        public void setPaths(List<String> paths) {
            this.paths = paths;
        }

        public List<String> getAccounts() {
            return accounts;
        }

        public void setAccounts(List<String> accounts) {
            this.accounts = accounts;
        }
    }

    public static class XAGetTransactionIDRequest {
        private String path;
        private String account;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getAccount() {
            return account;
        }

        public void setAccount(String account) {
            this.account = account;
        }
    }

    @Override
    public void handle(String uri, String httpMethod, String content, Callback callback) {
        RestResponse<Object> restResponse = new RestResponse<Object>();

        try {
            String method = uri.substring(1);

            switch (method) {
                case "startTransaction":
                    {
                        RestRequest<XAStartTransactionRequest> xaRequest =
                                objectMapper.readValue(
                                        content,
                                        new TypeReference<
                                                RestRequest<XAStartTransactionRequest>>() {});

                        List<String> accounts = xaRequest.getData().getAccounts();
                        Map<String, Account> accountMap = new HashMap<>();
                        for (String account : accounts) {
                            accountMap.put(
                                    accountManager.getAccount(account).getType(),
                                    accountManager.getAccount(account));
                        }

                        Set<Path> paths =
                                xaRequest
                                        .getData()
                                        .getPaths()
                                        .parallelStream()
                                        .map(
                                                (s) -> {
                                                    try {
                                                        return Path.decode(s);
                                                    } catch (Exception e) {
                                                        logger.error("error", e);
                                                        return null;
                                                    }
                                                })
                                        .collect(Collectors.toSet());

                        xaTransactionManager.asyncStartTransaction(
                                xaRequest.getData().getTransactionID(),
                                accountMap,
                                paths,
                                (e, result) -> {
                                    if (e != null) {
                                        logger.error("Error while startTransaction", e);

                                        restResponse.setErrorCode(
                                                NetworkQueryStatus.TRANSACTION_ERROR);
                                        restResponse.setMessage(e.getMessage());
                                        callback.onResponse(restResponse);

                                        return;
                                    }

                                    restResponse.setData(result);
                                    callback.onResponse(restResponse);
                                });
                        return;
                    }
                case "commitTransaction":
                    {
                        RestRequest<XACommitTransactionRequest> xaRequest =
                                objectMapper.readValue(
                                        content,
                                        new TypeReference<
                                                RestRequest<XACommitTransactionRequest>>() {});

                        List<String> accounts = xaRequest.getData().getAccounts();
                        Map<String, Account> accountMap = new HashMap<>();
                        for (String account : accounts) {
                            accountMap.put(
                                    accountManager.getAccount(account).getType(),
                                    accountManager.getAccount(account));
                        }

                        Set<Path> paths =
                                xaRequest
                                        .getData()
                                        .getPaths()
                                        .parallelStream()
                                        .map(
                                                (s) -> {
                                                    try {
                                                        return Path.decode(s);
                                                    } catch (Exception e) {
                                                        logger.error("error", e);
                                                        return null;
                                                    }
                                                })
                                        .collect(Collectors.toSet());

                        xaTransactionManager.asyncCommitTransaction(
                                xaRequest.getData().getTransactionID(),
                                accountMap,
                                paths,
                                (e, result) -> {
                                    if (e != null) {
                                        logger.error("Error while commitTransaction", e);

                                        restResponse.setErrorCode(
                                                NetworkQueryStatus.TRANSACTION_ERROR);
                                        restResponse.setMessage(e.getMessage());
                                        callback.onResponse(restResponse);

                                        return;
                                    }

                                    restResponse.setData(result);
                                    callback.onResponse(restResponse);
                                });

                        return;
                    }
                case "rollbackTransaction":
                    {
                        RestRequest<XARollbackTransactionRequest> xaRequest =
                                objectMapper.readValue(
                                        content,
                                        new TypeReference<
                                                RestRequest<XARollbackTransactionRequest>>() {});

                        List<String> accounts = xaRequest.getData().getAccounts();
                        Map<String, Account> accountMap = new HashMap<>();
                        for (String account : accounts) {
                            accountMap.put(
                                    accountManager.getAccount(account).getType(),
                                    accountManager.getAccount(account));
                        }

                        Set<Path> paths =
                                xaRequest
                                        .getData()
                                        .getPaths()
                                        .parallelStream()
                                        .map(
                                                (s) -> {
                                                    try {
                                                        return Path.decode(s);
                                                    } catch (Exception e) {
                                                        logger.error("error", e);
                                                        return null;
                                                    }
                                                })
                                        .collect(Collectors.toSet());

                        xaTransactionManager.asyncRollback(
                                xaRequest.getData().getTransactionID(),
                                accountMap,
                                paths,
                                (e, result) -> {
                                    if (e != null) {
                                        logger.error("Error while rollbackTransaction", e);

                                        restResponse.setErrorCode(
                                                NetworkQueryStatus.TRANSACTION_ERROR);
                                        restResponse.setMessage(e.getMessage());
                                        callback.onResponse(restResponse);

                                        return;
                                    }

                                    restResponse.setData(result);
                                    callback.onResponse(restResponse);
                                });

                        return;
                    }
                case "getTransactionInfo":
                    {
                        RestRequest<XAGetTransactionInfoRequest> xaRequest =
                                objectMapper.readValue(
                                        content,
                                        new TypeReference<
                                                RestRequest<XAGetTransactionInfoRequest>>() {});

                        List<String> accounts = xaRequest.getData().getAccounts();
                        Map<String, Account> accountMap = new HashMap<>();
                        for (String account : accounts) {
                            accountMap.put(
                                    accountManager.getAccount(account).getType(),
                                    accountManager.getAccount(account));
                        }

                        Set<Path> paths =
                                xaRequest
                                        .getData()
                                        .getPaths()
                                        .parallelStream()
                                        .map(
                                                (s) -> {
                                                    try {
                                                        return Path.decode(s);
                                                    } catch (Exception e) {
                                                        logger.error("error", e);
                                                        return null;
                                                    }
                                                })
                                        .collect(Collectors.toSet());

                        xaTransactionManager.asyncGetTransactionInfo(
                                xaRequest.getData().getTransactionID(),
                                accountMap,
                                paths,
                                (e, info) -> {
                                    if (e != null) {
                                        logger.error("Error while getTransactionInfo", e);

                                        restResponse.setErrorCode(
                                                NetworkQueryStatus.TRANSACTION_ERROR);
                                        restResponse.setMessage(e.getMessage());
                                        callback.onResponse(restResponse);

                                        return;
                                    }

                                    restResponse.setData(info.toString());
                                    callback.onResponse(restResponse);
                                });

                        return;
                    }
                case "getTransactionIDs":
                    {
                        RestRequest<XAGetTransactionIDRequest> xaRequest =
                                objectMapper.readValue(
                                        content,
                                        new TypeReference<
                                                RestRequest<XAGetTransactionIDRequest>>() {});

                        Path path = Path.decode(xaRequest.getData().getPath());
                        Path proxyPath = new Path(path);
                        proxyPath.setResource("WeCrossProxy");
                        Resource resource =
                                xaTransactionManager.getZoneManager().fetchResource(proxyPath);

                        Account account =
                                accountManager.getAccount(xaRequest.getData().getAccount());
                        if (Objects.isNull(account)) {
                            String errorMsg =
                                    "account not found: " + xaRequest.getData().getAccount();
                            logger.error(errorMsg);

                            restResponse.setErrorCode(NetworkQueryStatus.ACCOUNT_ERROR);
                            restResponse.setMessage(errorMsg);
                            callback.onResponse(restResponse);
                            return;
                        }

                        TransactionRequest transactionRequest = new TransactionRequest();
                        transactionRequest.setMethod("getTransactionIDs");
                        transactionRequest.getOptions().put(Resource.RAW_TRANSACTION, true);

                        resource.asyncCall(
                                transactionRequest,
                                account,
                                (error, response) -> {
                                    if (logger.isDebugEnabled()) {
                                        logger.debug(
                                                "getTransactionIDs response: {}, exception: ",
                                                response,
                                                error);
                                    }

                                    if (error != null && !error.isSuccess()) {
                                        restResponse.setErrorCode(
                                                NetworkQueryStatus.TRANSACTION_ERROR
                                                        + error.getErrorCode());
                                        restResponse.setMessage(error.getMessage());
                                    }

                                    if (response.getErrorCode() != 0) {
                                        logger.error(
                                                "getTransactionIDs failed: {} {}",
                                                response.getErrorCode(),
                                                response.getErrorMessage());

                                        restResponse.setErrorCode(
                                                NetworkQueryStatus.TRANSACTION_ERROR
                                                        + response.getErrorCode());
                                        restResponse.setMessage(response.getErrorMessage());
                                        return;
                                    }

                                    restResponse.setData(response.getResult()[0].trim().split(" "));
                                    callback.onResponse(restResponse);
                                });

                        return;
                    }
                default:
                    {
                        logger.warn("Unsupported method: {}", method);
                        restResponse.setErrorCode(NetworkQueryStatus.METHOD_ERROR);
                        restResponse.setMessage("Unsupported method: " + method);
                        break;
                    }
            }

        } catch (Exception e) {
            logger.error("Error while process xa", e);

            restResponse.setErrorCode(NetworkQueryStatus.TRANSACTION_ERROR);
            restResponse.setMessage("Undefined error");
        }

        callback.onResponse(restResponse);
    }

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public void setAccountManager(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    public XATransactionManager getXaTransactionManager() {
        return xaTransactionManager;
    }

    public void setXaTransactionManager(XATransactionManager xaTransactionManager) {
        this.xaTransactionManager = xaTransactionManager;
    }
}
