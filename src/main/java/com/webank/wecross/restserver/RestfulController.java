package com.webank.wecross.restserver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.account.AccountManager;
import com.webank.wecross.common.QueryStatus;
import com.webank.wecross.exception.ErrorCode;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.restserver.request.ResourceRequest;
import com.webank.wecross.restserver.request.StateRequest;
import com.webank.wecross.restserver.response.AccountResponse;
import com.webank.wecross.restserver.response.ResourceResponse;
import com.webank.wecross.restserver.response.StateResponse;
import com.webank.wecross.restserver.response.StubResponse;
import com.webank.wecross.routine.htlc.HTLCManager;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.StubManager;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import com.webank.wecross.zone.ZoneManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestfulController {

    @javax.annotation.Resource private WeCrossHost host;

    private Logger logger = LoggerFactory.getLogger(RestfulController.class);
    private ObjectMapper objectMapper = new ObjectMapper();

    @RequestMapping("/test")
    public String test() {
        return "OK!";
    }

    @RequestMapping(value = "/supportedStubs", method = RequestMethod.POST)
    public RestResponse<StubResponse> supportedStubs(@RequestBody String restRequestString) {
        RestResponse<StubResponse> restResponse = new RestResponse<>();
        restResponse.setVersion(Versions.currentVersion);
        restResponse.setResult(QueryStatus.SUCCESS);
        restResponse.setMessage(QueryStatus.getStatusMessage(QueryStatus.SUCCESS));

        logger.debug("request string: {}", restRequestString);

        try {
            RestRequest restRequest =
                    objectMapper.readValue(restRequestString, new TypeReference<RestRequest>() {});
            restRequest.checkRestRequest("", "supportedStubs");
            StubResponse stubResponse = new StubResponse();
            ZoneManager zoneManager = host.getZoneManager();
            StubManager stubManager = zoneManager.getStubManager();
            stubResponse.setStubs(stubManager);
            restResponse.setData(stubResponse);
        } catch (WeCrossException e) {
            logger.warn("Process request error: {}", e.getMessage());
            restResponse.setResult(QueryStatus.EXCEPTION_FLAG + e.getErrorCode());
            restResponse.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.warn("Process request error: {}", e.getMessage());
            restResponse.setResult(QueryStatus.INTERNAL_ERROR);
            restResponse.setMessage(e.getMessage());
        }
        return restResponse;
    }

    @RequestMapping(value = "/listResources", method = RequestMethod.POST)
    public RestResponse<ResourceResponse> listResources(@RequestBody String restRequestString) {
        RestResponse<ResourceResponse> restResponse = new RestResponse<>();
        restResponse.setVersion(Versions.currentVersion);
        restResponse.setResult(QueryStatus.SUCCESS);
        restResponse.setMessage(QueryStatus.getStatusMessage(QueryStatus.SUCCESS));

        logger.debug("request string: {}", restRequestString);

        try {
            RestRequest<ResourceRequest> restRequest =
                    objectMapper.readValue(
                            restRequestString,
                            new TypeReference<RestRequest<ResourceRequest>>() {});
            restRequest.checkRestRequest("", "listResources");
            ResourceRequest resourceRequest = restRequest.getData();
            ZoneManager zoneManager = host.getZoneManager();
            ResourceResponse resourceResponse = new ResourceResponse();
            resourceResponse.setResourceInfos(zoneManager, resourceRequest.isIgnoreRemote());
            restResponse.setData(resourceResponse);
        } catch (WeCrossException e) {
            logger.warn("Process request error: {}", e.getMessage());
            restResponse.setResult(QueryStatus.EXCEPTION_FLAG + e.getErrorCode());
            restResponse.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.warn("Process request error: {}", e.getMessage());
            restResponse.setResult(QueryStatus.INTERNAL_ERROR);
            restResponse.setMessage(e.getMessage());
        }
        return restResponse;
    }

    @RequestMapping(value = "/listAccounts", method = RequestMethod.POST)
    public RestResponse<AccountResponse> listAccounts(@RequestBody String restRequestString) {
        RestResponse<AccountResponse> restResponse = new RestResponse<>();
        restResponse.setVersion(Versions.currentVersion);
        restResponse.setResult(QueryStatus.SUCCESS);
        restResponse.setMessage(QueryStatus.getStatusMessage(QueryStatus.SUCCESS));

        logger.debug("request string: {}", restRequestString);

        try {
            AccountManager accountManager = host.getAccountManager();
            RestRequest restRequest =
                    objectMapper.readValue(restRequestString, new TypeReference<RestRequest>() {});
            restRequest.checkRestRequest("", "listAccounts");
            AccountResponse accountResponse = new AccountResponse();
            accountResponse.setAccountInfos(accountManager);
            restResponse.setData(accountResponse);
        } catch (WeCrossException e) {
            logger.warn("Process request error: {}", e.getMessage());
            restResponse.setResult(QueryStatus.EXCEPTION_FLAG + e.getErrorCode());
            restResponse.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.warn("Process request error: {}", e.getMessage());
            restResponse.setResult(QueryStatus.INTERNAL_ERROR);
            restResponse.setMessage(e.getMessage());
        }
        return restResponse;
    }

    @RequestMapping(value = "/state")
    public RestResponse<StateResponse> handlesState() {
        RestResponse<StateResponse> restResponse = new RestResponse<StateResponse>();

        StateResponse stateResponse = host.getState(new StateRequest());
        restResponse.setVersion(Versions.currentVersion);
        restResponse.setResult(QueryStatus.SUCCESS);
        restResponse.setMessage(QueryStatus.getStatusMessage(QueryStatus.SUCCESS));
        restResponse.setData(stateResponse);

        return restResponse;
    }

    @RequestMapping(value = "/{network}/{stub}/{resource}/{method}", method = RequestMethod.GET)
    public RestResponse<Object> handleResource(
            @PathVariable("network") String network,
            @PathVariable("stub") String stub,
            @PathVariable("resource") String resource,
            @PathVariable("method") String method) {
        return handleResource(network, stub, resource, method, "");
    }

    @RequestMapping(
            value = {
                "/{network}/{stub}/{resource}/{method}",
            },
            method = RequestMethod.POST)
    public RestResponse<Object> handleResource(
            @PathVariable("network") String network,
            @PathVariable("stub") String stub,
            @PathVariable("resource") String resource,
            @PathVariable("method") String method,
            @RequestBody String restRequestString) {
        Path path = new Path();
        path.setNetwork(network);
        path.setChain(stub);
        path.setResource(resource);

        RestResponse<Object> restResponse = new RestResponse<Object>();
        restResponse.setVersion(Versions.currentVersion);
        restResponse.setResult(QueryStatus.SUCCESS);
        restResponse.setMessage(QueryStatus.getStatusMessage(QueryStatus.SUCCESS));

        logger.debug("request string: {}", restRequestString);

        try {
            AccountManager accountManager = host.getAccountManager();
            HTLCManager htlcManager = host.getHtlcManager();

            Resource resourceObj = host.getResource(path);
            resourceObj = htlcManager.filterHTLCResource(path, resourceObj);

            if (resourceObj == null) {
                logger.warn("Unable to find resource: {}", path.toString());
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
                                    ErrorCode.RESOURCE_ERROR, "Resource not found");
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
                                    ErrorCode.RESOURCE_ERROR, "Resource not found");
                        }

                        RestRequest<TransactionRequest> restRequest =
                                objectMapper.readValue(
                                        restRequestString,
                                        new TypeReference<RestRequest<TransactionRequest>>() {});

                        restRequest.checkRestRequest(path.toString(), method);

                        TransactionRequest transactionRequest =
                                (TransactionRequest) restRequest.getData();

                        Account account = accountManager.getAccount(restRequest.getAccountName());

                        TransactionResponse transactionResponse =
                                (TransactionResponse)
                                        resourceObj.call(
                                                new TransactionContext<TransactionRequest>(
                                                        transactionRequest,
                                                        account,
                                                        resourceObj.getResourceInfo()));

                        restResponse.setData(transactionResponse);
                        break;
                    }
                case "sendTransaction":
                    {
                        if (resourceObj == null) {
                            throw new WeCrossException(
                                    ErrorCode.RESOURCE_ERROR, "Resource not found");
                        }
                        RestRequest<TransactionRequest> restRequest =
                                objectMapper.readValue(
                                        restRequestString,
                                        new TypeReference<RestRequest<TransactionRequest>>() {});

                        restRequest.checkRestRequest(path.toString(), method);

                        TransactionRequest transactionRequest =
                                (TransactionRequest) restRequest.getData();

                        Account account = accountManager.getAccount(restRequest.getAccountName());

                        TransactionResponse transactionResponse =
                                (TransactionResponse)
                                        resourceObj.sendTransaction(
                                                new TransactionContext<TransactionRequest>(
                                                        transactionRequest,
                                                        account,
                                                        resourceObj.getResourceInfo()));

                        restResponse.setData(transactionResponse);
                        break;
                    }
                default:
                    {
                        logger.warn("Unsupported method: {}", method);
                        restResponse.setResult(QueryStatus.METHOD_ERROR);
                        restResponse.setMessage("Unsupported method: " + method);
                        break;
                    }
            }
        } catch (WeCrossException e) {
            logger.warn("Process request error: {}", e.getMessage());
            restResponse.setResult(QueryStatus.EXCEPTION_FLAG + e.getErrorCode());
            restResponse.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.warn("Process request error:", e);
            restResponse.setResult(QueryStatus.INTERNAL_ERROR);
            restResponse.setMessage(e.getLocalizedMessage());
        }

        return restResponse;
    }
}
