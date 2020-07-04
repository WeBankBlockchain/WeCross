package com.webank.wecross.network.rpc.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.account.AccountManager;
import com.webank.wecross.common.NetworkQueryStatus;
import com.webank.wecross.restserver.RestRequest;
import com.webank.wecross.restserver.RestResponse;
import com.webank.wecross.routine.xa.XATransactionInfo;
import com.webank.wecross.routine.xa.XATransactionManager;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public static class XAStartTransactionResponse {
        private int result;

        public int getResult() {
            return result;
        }

        public void setResult(int result) {
            this.result = result;
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

    public static class XACommitTransactionResponse {
        private int result;

        public int getResult() {
            return result;
        }

        public void setResult(int result) {
            this.result = result;
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

    public static class XARollbackTransactionResponse {
        private int result;

        public int getResult() {
            return result;
        }

        public void setResult(int result) {
            this.result = result;
        }
    }

    public static class XAGetTransactionInfoRequest {
        private String transactionID;
        private List<String> chains;
        private List<String> accounts;

        public String getTransactionID() {
            return transactionID;
        }

        public void setTransactionID(String transactionID) {
            this.transactionID = transactionID;
        }

        public List<String> getChains() {
            return chains;
        }

        public void setChains(List<String> chains) {
            this.chains = chains;
        }

        public List<String> getAccounts() {
            return accounts;
        }

        public void setAccounts(List<String> accounts) {
            this.accounts = accounts;
        }
    }

    public static class XAGetTransactionInfoResponse {
        private XATransactionInfo info;

        public XATransactionInfo getInfo() {
            return info;
        }

        public void setInfo(XATransactionInfo info) {
            this.info = info;
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
                                        restResponse.setMessage("Error while startTransaction");
                                        callback.onResponse(restResponse);

                                        return;
                                    }

                                    XAStartTransactionResponse response =
                                            new XAStartTransactionResponse();
                                    response.setResult(result);

                                    callback.onResponse(restResponse);
                                });
                        break;
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
                                        restResponse.setMessage("Error while startTransaction");
                                        callback.onResponse(restResponse);

                                        return;
                                    }

                                    XACommitTransactionResponse response =
                                            new XACommitTransactionResponse();
                                    response.setResult(result);

                                    callback.onResponse(restResponse);
                                });

                        break;
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
                                        restResponse.setMessage("Error while startTransaction");
                                        callback.onResponse(restResponse);

                                        return;
                                    }

                                    XARollbackTransactionResponse response =
                                            new XARollbackTransactionResponse();
                                    response.setResult(result);

                                    callback.onResponse(restResponse);
                                });

                        break;
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
                                        .getChains()
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
                                        restResponse.setMessage("Error while startTransaction");
                                        callback.onResponse(restResponse);

                                        return;
                                    }

                                    XAGetTransactionInfoResponse response =
                                            new XAGetTransactionInfoResponse();
                                    response.setInfo(info);

                                    callback.onResponse(restResponse);
                                });

                        break;
                    }
            }
        } catch (Exception e) {
            logger.error("Error while process xa", e);

            restResponse.setErrorCode(NetworkQueryStatus.TRANSACTION_ERROR);
            restResponse.setMessage("Error while startTransaction");
            callback.onResponse(restResponse);
        }
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
