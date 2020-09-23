package com.webank.wecross.network.rpc.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.account.UserContext;
import com.webank.wecross.common.NetworkQueryStatus;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.restserver.RestRequest;
import com.webank.wecross.restserver.RestResponse;
import com.webank.wecross.routine.xa.XATransactionManager;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.StubConstant;
import com.webank.wecross.stub.UniversalAccount;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XATransactionHandler implements URIHandler {
    private Logger logger = LoggerFactory.getLogger(XATransactionHandler.class);
    private ObjectMapper objectMapper = new ObjectMapper();
    private XATransactionManager xaTransactionManager;

    private WeCrossHost host;

    public static class XAStartTransactionRequest {
        private String transactionID;
        private List<String> paths;

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
    }

    public static class XACommitTransactionRequest {
        private String transactionID;
        private List<String> paths;

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
    }

    public static class XARollbackTransactionRequest {
        private String transactionID;
        private List<String> paths;

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
    }

    public static class XAGetTransactionInfoRequest {
        private String transactionID;
        private List<String> paths;

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
    }

    public static class XAGetTransactionIDRequest {
        private String path;
        private int option;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public int getOption() {
            return option;
        }

        public void setOption(int option) {
            this.option = option;
        }
    }

    @Override
    public void handle(
            UserContext userContext,
            String uri,
            String httpMethod,
            String content,
            Callback callback) {
        RestResponse<Object> restResponse = new RestResponse<Object>();

        try {
            UniversalAccount ua = host.getAccountManager().getUniversalAccount(userContext);
            String method = uri.substring(1);

            switch (method) {
                case "startTransaction":
                    {
                        RestRequest<XAStartTransactionRequest> xaRequest =
                                objectMapper.readValue(
                                        content,
                                        new TypeReference<
                                                RestRequest<XAStartTransactionRequest>>() {});

                        xaTransactionManager.asyncStartTransaction(
                                xaRequest.getData().getTransactionID(),
                                ua,
                                listPathsToSet(xaRequest.getData().getPaths()),
                                (e, result) -> {
                                    if (e != null) {
                                        logger.error("Error while startTransaction", e);

                                        restResponse.setErrorCode(
                                                NetworkQueryStatus.XA_ERROR + e.getErrorCode());
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

                        xaTransactionManager.asyncCommitTransaction(
                                xaRequest.getData().getTransactionID(),
                                ua,
                                listPathsToSet(xaRequest.getData().getPaths()),
                                (e, result) -> {
                                    if (e != null) {
                                        logger.error("Error while commitTransaction", e);

                                        restResponse.setErrorCode(
                                                NetworkQueryStatus.XA_ERROR + e.getErrorCode());
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

                        xaTransactionManager.asyncRollback(
                                xaRequest.getData().getTransactionID(),
                                ua,
                                listPathsToSet(xaRequest.getData().getPaths()),
                                (e, result) -> {
                                    if (e != null) {
                                        logger.error("Error while rollbackTransaction", e);

                                        restResponse.setErrorCode(
                                                NetworkQueryStatus.XA_ERROR + e.getErrorCode());
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

                        xaTransactionManager.asyncGetTransactionInfo(
                                xaRequest.getData().getTransactionID(),
                                ua,
                                listPathsToSet(xaRequest.getData().getPaths()),
                                (e, info) -> {
                                    if (e != null) {
                                        logger.error("Error while getTransactionInfo", e);

                                        restResponse.setErrorCode(
                                                NetworkQueryStatus.XA_ERROR + e.getErrorCode());
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
                        proxyPath.setResource(StubConstant.PROXY_NAME);
                        Resource resource =
                                xaTransactionManager.getZoneManager().fetchResource(proxyPath);

                        xaTransactionManager.asyncGetTransactionIDs(
                                resource,
                                ua,
                                xaRequest.getData().getOption(),
                                (exception, ids) -> {
                                    if (Objects.nonNull(exception)) {
                                        restResponse.setErrorCode(
                                                NetworkQueryStatus.XA_ERROR
                                                        + exception.getErrorCode());
                                        restResponse.setMessage(exception.getMessage());
                                        callback.onResponse(restResponse);
                                        return;
                                    }

                                    restResponse.setData(ids);
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
        } catch (WeCrossException e) {
            logger.error("Error while process xa", e);
            restResponse.setErrorCode(NetworkQueryStatus.EXCEPTION_FLAG + e.getErrorCode());
            restResponse.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("Error while process xa", e);
            restResponse.setErrorCode(NetworkQueryStatus.INTERNAL_ERROR);
            restResponse.setMessage("Undefined error");
        }

        callback.onResponse(restResponse);
    }

    private Set<Path> listPathsToSet(List<String> paths) {
        return paths.parallelStream()
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
    }

    public XATransactionManager getXaTransactionManager() {
        return xaTransactionManager;
    }

    public void setXaTransactionManager(XATransactionManager xaTransactionManager) {
        this.xaTransactionManager = xaTransactionManager;
    }

    public void setHost(WeCrossHost host) {
        this.host = host;
    }
}
