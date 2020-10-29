package com.webank.wecross.network.rpc.handler;

import static com.webank.wecross.exception.WeCrossException.ErrorCode.GET_UA_FAILED;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.account.UniversalAccount;
import com.webank.wecross.account.UserContext;
import com.webank.wecross.common.NetworkQueryStatus;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.restserver.RestRequest;
import com.webank.wecross.restserver.RestResponse;
import com.webank.wecross.routine.xa.XATransactionManager;
import com.webank.wecross.stub.ObjectMapperFactory;
import com.webank.wecross.stub.Path;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** POST/GET /xa/method */
public class XATransactionHandler implements URIHandler {
    private Logger logger = LoggerFactory.getLogger(XATransactionHandler.class);
    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    private XATransactionManager xaTransactionManager;

    private WeCrossHost host;

    public static class StartXATransactionRequest {
        private String xaTransactionID;
        private Set<String> paths;

        public String getXaTransactionID() {
            return xaTransactionID;
        }

        public void setXaTransactionID(String xaTransactionID) {
            this.xaTransactionID = xaTransactionID;
        }

        public Set<String> getPaths() {
            return paths;
        }

        public void setPaths(Set<String> paths) {
            this.paths = paths;
        }
    }

    public static class CommitXATransactionRequest {
        private String xaTransactionID;
        private Set<String> paths;

        public String getXaTransactionID() {
            return xaTransactionID;
        }

        public void setXaTransactionID(String xaTransactionID) {
            this.xaTransactionID = xaTransactionID;
        }

        public Set<String> getPaths() {
            return paths;
        }

        public void setPaths(Set<String> paths) {
            this.paths = paths;
        }
    }

    public static class RollbackXATransactionRequest {
        private String xaTransactionID;
        private Set<String> paths;

        public String getXaTransactionID() {
            return xaTransactionID;
        }

        public void setXaTransactionID(String xaTransactionID) {
            this.xaTransactionID = xaTransactionID;
        }

        public Set<String> getPaths() {
            return paths;
        }

        public void setPaths(Set<String> paths) {
            this.paths = paths;
        }
    }

    public static class GetXATransactionRequest {
        private String xaTransactionID;
        private Set<String> paths;

        public String getXaTransactionID() {
            return xaTransactionID;
        }

        public void setXaTransactionID(String xaTransactionID) {
            this.xaTransactionID = xaTransactionID;
        }

        public Set<String> getPaths() {
            return paths;
        }

        public void setPaths(Set<String> paths) {
            this.paths = paths;
        }
    }

    public static class ListXATransactionsRequest {
        private int size;
        private List<Integer> offsets = new ArrayList<>();

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public List<Integer> getOffsets() {
            return offsets;
        }

        public void setOffsets(List<Integer> offsets) {
            this.offsets = offsets;
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

        UniversalAccount ua;
        try {
            ua = host.getAccountManager().getUniversalAccount(userContext);
            if (ua == null) {
                throw new WeCrossException(GET_UA_FAILED, "Failed to get universal account");
            }
        } catch (WeCrossException e) {
            restResponse.setErrorCode(
                    NetworkQueryStatus.UNIVERSAL_ACCOUNT_ERROR + e.getErrorCode());
            restResponse.setMessage(e.getMessage());
            callback.onResponse(restResponse);
            return;
        }

        try {
            String[] splits = uri.substring(1).split("/");
            String method = splits[1];

            switch (method) {
                case "startXATransaction":
                    {
                        RestRequest<StartXATransactionRequest> xaRequest =
                                objectMapper.readValue(
                                        content,
                                        new TypeReference<
                                                RestRequest<StartXATransactionRequest>>() {});

                        xaTransactionManager.asyncStartXATransaction(
                                xaRequest.getData().getXaTransactionID(),
                                ua,
                                decodePathSet(xaRequest.getData().getPaths()),
                                (response) -> {
                                    restResponse.setData(response);
                                    callback.onResponse(restResponse);
                                });
                        return;
                    }
                case "commitXATransaction":
                    {
                        RestRequest<CommitXATransactionRequest> xaRequest =
                                objectMapper.readValue(
                                        content,
                                        new TypeReference<
                                                RestRequest<CommitXATransactionRequest>>() {});

                        xaTransactionManager.asyncCommitXATransaction(
                                xaRequest.getData().getXaTransactionID(),
                                ua,
                                filterAndSortChainPaths(xaRequest.getData().getPaths()),
                                (response) -> {
                                    restResponse.setData(response);
                                    callback.onResponse(restResponse);
                                });
                        return;
                    }
                case "rollbackXATransaction":
                    {
                        RestRequest<RollbackXATransactionRequest> xaRequest =
                                objectMapper.readValue(
                                        content,
                                        new TypeReference<
                                                RestRequest<RollbackXATransactionRequest>>() {});

                        xaTransactionManager.asyncRollbackXATransaction(
                                xaRequest.getData().getXaTransactionID(),
                                ua,
                                filterAndSortChainPaths(xaRequest.getData().getPaths()),
                                (response) -> {
                                    restResponse.setData(response);
                                    callback.onResponse(restResponse);
                                });
                        return;
                    }
                case "getXATransaction":
                    {
                        RestRequest<GetXATransactionRequest> xaRequest =
                                objectMapper.readValue(
                                        content,
                                        new TypeReference<
                                                RestRequest<GetXATransactionRequest>>() {});

                        xaTransactionManager.asyncGetXATransaction(
                                xaRequest.getData().getXaTransactionID(),
                                ua,
                                filterAndSortChainPaths(xaRequest.getData().getPaths()),
                                (xaTransactionResponse) -> {
                                    restResponse.setData(xaTransactionResponse);
                                    callback.onResponse(restResponse);
                                });

                        return;
                    }
                case "listXATransactions":
                    {
                        RestRequest<ListXATransactionsRequest> xaRequest =
                                objectMapper.readValue(
                                        content,
                                        new TypeReference<
                                                RestRequest<ListXATransactionsRequest>>() {});

                        xaTransactionManager.asyncGetTransactionIDs(
                                ua,
                                xaRequest.getData().getSize(),
                                xaRequest.getData().getOffsets(),
                                (exception, xaTransactionListResponse) -> {
                                    if (Objects.nonNull(exception)) {
                                        restResponse.setErrorCode(
                                                NetworkQueryStatus.XA_ERROR
                                                        + exception.getErrorCode());
                                        restResponse.setMessage(exception.getMessage());
                                        callback.onResponse(restResponse);
                                        return;
                                    }

                                    restResponse.setData(xaTransactionListResponse);
                                    callback.onResponse(restResponse);
                                });
                        return;
                    }
                default:
                    {
                        logger.warn("Unsupported method: {}", method);
                        restResponse.setErrorCode(NetworkQueryStatus.URI_PATH_ERROR);
                        restResponse.setMessage("Unsupported method: " + method);
                        break;
                    }
            }
        } catch (WeCrossException e) {
            logger.error("Error while processing xa: ", e);
            restResponse.setErrorCode(NetworkQueryStatus.XA_ERROR + e.getErrorCode());
            restResponse.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("Error while processing xa: ", e);
            restResponse.setErrorCode(NetworkQueryStatus.INTERNAL_ERROR);
            restResponse.setMessage("Undefined error: " + e.getMessage());
        }

        callback.onResponse(restResponse);
    }

    private Set<Path> decodePathSet(Set<String> paths) throws WeCrossException {
        Set<Path> res =
                paths.parallelStream()
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

        if (res.isEmpty() || res.size() < paths.size()) {
            throw new WeCrossException(
                    WeCrossException.ErrorCode.POST_DATA_ERROR, "Invalid path found");
        }

        return res;
    }

    public Set<Path> filterAndSortChainPaths(Set<String> paths) throws WeCrossException {
        Set<String> tempPaths = new HashSet<>();
        for (String path : paths) {
            try {
                String[] splits = path.split("\\.");
                tempPaths.add(splits[0] + "." + splits[1]);
            } catch (Exception e) {
                throw new WeCrossException(
                        WeCrossException.ErrorCode.POST_DATA_ERROR, "Invalid path found");
            }
        }

        Set<Path> sortSet = new TreeSet<>(Comparator.comparing(Path::toString));
        sortSet.addAll(decodePathSet(tempPaths));
        return sortSet;
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
