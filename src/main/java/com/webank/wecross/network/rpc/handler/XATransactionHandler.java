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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XATransactionHandler implements URIHandler {
    private Logger logger = LoggerFactory.getLogger(XATransactionHandler.class);
    private ObjectMapper objectMapper = new ObjectMapper();
    private AccountManager accountManager;
    private XATransactionManager xaTransactionManager;

    public class XAPrepareRequest {
        private String transactionID;
        private List<String> resources;

        public String getTransactionID() {
            return transactionID;
        }

        public void setTransactionID(String transactionID) {
            this.transactionID = transactionID;
        }

        public List<String> getResources() {
            return resources;
        }

        public void setResources(List<String> resources) {
            this.resources = resources;
        }
    }

    public class XAPrepareResponse {
        private int result;

        public int getResult() {
            return result;
        }

        public void setResult(int result) {
            this.result = result;
        }
    }

    public class XACommitRequest {
        private String transactionID;
        private List<String> chains;

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
    }

    public class XACommitResponse {
        private int result;

        public int getResult() {
            return result;
        }

        public void setResult(int result) {
            this.result = result;
        }
    }

    public class XARollbackRequest {
        private String transactionID;
        private List<String> chains;

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
    }

    public class XARollbackResponse {
        private int result;

        public int getResult() {
            return result;
        }

        public void setResult(int result) {
            this.result = result;
        }
    }

    public class XAGetTransactionInfoRequest {
        private String transactionID;
        private String chain;

        public String getTransactionID() {
            return transactionID;
        }

        public void setTransactionID(String transactionID) {
            this.transactionID = transactionID;
        }

        public String getChain() {
            return chain;
        }

        public void setChain(String chain) {
            this.chain = chain;
        }
    }

    public class XAGetTransactionInfoResponse {
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
                        RestRequest<XAPrepareRequest> xaRequest =
                                objectMapper.readValue(
                                        content,
                                        new TypeReference<RestRequest<XAPrepareRequest>>() {});

                        Account account = accountManager.getAccount(xaRequest.getAccountName());

                        Set<Path> paths =
                                xaRequest
                                        .getData()
                                        .getResources()
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

                        xaTransactionManager.asyncPrepare(
                                xaRequest.getData().getTransactionID(),
                                account,
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

                                    XAPrepareResponse response = new XAPrepareResponse();
                                    response.setResult(result);

                                    callback.onResponse(restResponse);
                                });
                        break;
                    }
                case "commitTransaction":
                    {
                        RestRequest<XACommitRequest> xaRequest =
                                objectMapper.readValue(
                                        content,
                                        new TypeReference<RestRequest<XACommitRequest>>() {});

                        Account account = accountManager.getAccount(xaRequest.getAccountName());

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

                        xaTransactionManager.asyncCommit(
                                xaRequest.getData().getTransactionID(),
                                account,
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

                                    XACommitResponse response = new XACommitResponse();
                                    response.setResult(result);

                                    callback.onResponse(restResponse);
                                });

                        break;
                    }
                case "rollback":
                    {
                        RestRequest<XARollbackRequest> xaRequest =
                                objectMapper.readValue(
                                        content,
                                        new TypeReference<RestRequest<XARollbackRequest>>() {});

                        Account account = accountManager.getAccount(xaRequest.getAccountName());

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

                        xaTransactionManager.asyncRollback(
                                xaRequest.getData().getTransactionID(),
                                account,
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

                                    XARollbackResponse response = new XARollbackResponse();
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

                        Account account = accountManager.getAccount(xaRequest.getAccountName());

                        Path path = null;
                        try {
                            path = Path.decode(xaRequest.getData().getChain());
                        } catch (Exception e) {
                        }

                        xaTransactionManager.asyncGetTransactionInfo(
                                xaRequest.getData().getTransactionID(),
                                path,
                                account,
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
