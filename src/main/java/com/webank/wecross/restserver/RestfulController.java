package com.webank.wecross.restserver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.chain.StateRequest;
import com.webank.wecross.chain.StateResponse;
import com.webank.wecross.common.QueryStatus;
import com.webank.wecross.exception.ErrorCode;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.restserver.request.ResourceRequest;
import com.webank.wecross.restserver.response.ResourceResponse;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import com.webank.wecross.zone.ZoneManager;
import java.util.List;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
// @SpringBootApplication
public class RestfulController {

    @javax.annotation.Resource(name = "newWeCrossHost")
    private WeCrossHost host;

    private Logger logger = LoggerFactory.getLogger(RestfulController.class);
    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    @RequestMapping("/test")
    public String test() {
        return "OK!";
    }

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public RestResponse<ResourceResponse> handleList(@RequestBody String restRequestString) {
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

            restRequest.checkRestRequest("", "list");

            ResourceRequest resourceRequest = restRequest.getData();
            ZoneManager networkManager = host.getZoneManager();

            ResourceResponse resourceResponse = new ResourceResponse();
            try {
                List<Resource> resources =
                        networkManager.getAllResources(resourceRequest.isIgnoreRemote());
                resourceResponse.setErrorCode(0);
                resourceResponse.setErrorMessage("");
                resourceResponse.setResources(resources);
            } catch (Exception e) {
                resourceResponse.setErrorCode(1);
                resourceResponse.setErrorMessage("Unexpected error: " + e.getMessage());
            }

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
            Resource resourceObj = host.getResource(path);
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
                case "info":
                    {
                        if (resourceObj == null) {
                            throw new WeCrossException(
                                    ErrorCode.RESOURCE_ERROR, "Resource not found");
                        } else {
                            restResponse.setData(resourceObj);
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
                        TransactionResponse transactionResponse =
                                (TransactionResponse) resourceObj.call(transactionRequest);

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
                        TransactionResponse transactionResponse =
                                (TransactionResponse)
                                        resourceObj.sendTransaction(transactionRequest);

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
