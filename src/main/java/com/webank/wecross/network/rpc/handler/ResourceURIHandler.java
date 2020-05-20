package com.webank.wecross.network.rpc.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.account.AccountManager;
import com.webank.wecross.common.NetworkQueryStatus;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.resource.ResourceDetail;
import com.webank.wecross.restserver.RestRequest;
import com.webank.wecross.restserver.RestResponse;
import com.webank.wecross.routine.htlc.HTLCManager;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionException;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** GET/POST /network/stub/resource/method */
public class ResourceURIHandler implements URIHandler {

    private static final Logger logger = LoggerFactory.getLogger(ResourceURIHandler.class);

    private WeCrossHost host;
    private ObjectMapper objectMapper = new ObjectMapper();

    public ResourceURIHandler(WeCrossHost host) {
        this.host = host;
    }

    public WeCrossHost getHost() {
        return host;
    }

    public void setHost(WeCrossHost host) {
        this.host = host;
    }

    @Override
    public RestResponse handle(ChannelHandlerContext ctx, FullHttpRequest httpRequest) {
        RestResponse<Object> restResponse = new RestResponse<>();
        try {
            String content = httpRequest.content().toString(StandardCharsets.UTF_8);
            Path path = Path.fromURI(httpRequest.uri());
            String method = httpRequest.uri().split("/")[3];

            if (logger.isDebugEnabled()) {
                logger.debug("request string: {}", content);
            }

            AccountManager accountManager = host.getAccountManager();
            Resource resourceObj = host.getResource(path);
            if (resourceObj == null) {
                logger.warn("Unable to find resource: {}", path);
            } else {
                HTLCManager htlcManager = host.getRoutineManager().getHtlcManager();
                resourceObj =
                        htlcManager.filterHTLCResource(host.getZoneManager(), path, resourceObj);
            }

            switch (method) {
                case "status":
                    {
                        if (resourceObj == null) {
                            restResponse.setData("not exists");
                        } else {
                            restResponse.setData("exists");
                        }
                        break;
                    }
                case "detail":
                    {
                        if (resourceObj == null) {
                            throw new WeCrossException(
                                    WeCrossException.ErrorCode.RESOURCE_ERROR,
                                    "Resource not found");
                        } else {
                            ResourceDetail resourceDetail = new ResourceDetail();
                            restResponse.setData(
                                    resourceDetail.initResourceDetail(
                                            resourceObj, path.toString()));
                        }
                        break;
                    }
                case "call":
                    {
                        if (resourceObj == null) {
                            throw new WeCrossException(
                                    WeCrossException.ErrorCode.RESOURCE_ERROR,
                                    "Resource not found");
                        }

                        RestRequest<TransactionRequest> restRequest =
                                objectMapper.readValue(
                                        content,
                                        new TypeReference<RestRequest<TransactionRequest>>() {});

                        restRequest.checkRestRequest(path.toString(), method);

                        TransactionRequest transactionRequest = restRequest.getData();

                        String accountName = restRequest.getAccountName();
                        Account account = accountManager.getAccount(accountName);
                        logger.trace(
                                "call request: {}, account: {}", transactionRequest, accountName);

                        TransactionResponse transactionResponse =
                                resourceObj.call(
                                        new TransactionContext<TransactionRequest>(
                                                transactionRequest,
                                                account,
                                                resourceObj.getResourceInfo(),
                                                resourceObj.getResourceBlockHeaderManager()));
                        logger.trace("call response: {}", transactionResponse);
                        restResponse.setData(transactionResponse);
                        break;
                    }
                case "sendTransaction":
                    {
                        if (resourceObj == null) {
                            throw new WeCrossException(
                                    WeCrossException.ErrorCode.RESOURCE_ERROR,
                                    "Resource not found");
                        }
                        RestRequest<TransactionRequest> restRequest =
                                objectMapper.readValue(
                                        content,
                                        new TypeReference<RestRequest<TransactionRequest>>() {});

                        restRequest.checkRestRequest(path.toString(), method);

                        TransactionRequest transactionRequest = restRequest.getData();
                        String accountName = restRequest.getAccountName();
                        Account account = accountManager.getAccount(accountName);
                        logger.trace(
                                "sendTransaction request: {}, account: {}",
                                transactionRequest,
                                accountName);

                        TransactionResponse transactionResponse =
                                resourceObj.sendTransaction(
                                        new TransactionContext<>(
                                                transactionRequest,
                                                account,
                                                resourceObj.getResourceInfo(),
                                                resourceObj.getResourceBlockHeaderManager()));
                        logger.trace("sendTransaction response: {}", transactionResponse);
                        restResponse.setData(transactionResponse);
                        break;
                    }
                default:
                    {
                        logger.warn("Unsupported method: {}", method);
                        restResponse.setErrorCode(NetworkQueryStatus.METHOD_ERROR);
                        restResponse.setMessage("Unsupported method: " + method);
                        break;
                    }
            }
        } catch (TransactionException e) {
            logger.warn("TransactionException error", e);
            restResponse.setErrorCode(NetworkQueryStatus.TRANSACTION_ERROR + e.getErrorCode());
            restResponse.setMessage(e.getMessage());
        } catch (WeCrossException e) {
            logger.warn("Process request error", e);
            restResponse.setErrorCode(NetworkQueryStatus.EXCEPTION_FLAG + e.getErrorCode());
            restResponse.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.warn("Process request error:", e);
            restResponse.setErrorCode(NetworkQueryStatus.INTERNAL_ERROR);
            restResponse.setMessage(e.getLocalizedMessage());
        }

        return restResponse;
    }
}
