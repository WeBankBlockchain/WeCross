package com.webank.wecross.restserver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.common.QueryStatus;
import com.webank.wecross.exception.ErrorCode;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.network.NetworkManager;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.restserver.request.GetDataRequest;
import com.webank.wecross.restserver.request.ResourceRequest;
import com.webank.wecross.restserver.request.SetDataRequest;
import com.webank.wecross.restserver.request.TransactionRequest;
import com.webank.wecross.restserver.response.GetDataResponse;
import com.webank.wecross.restserver.response.ResourceResponse;
import com.webank.wecross.restserver.response.SetDataResponse;
import com.webank.wecross.restserver.response.TransactionResponse;
import com.webank.wecross.stub.StateRequest;
import com.webank.wecross.stub.StateResponse;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class RestfulService {

    @javax.annotation.Resource(name = "newWeCrossHost")
    private WeCrossHost host;

    private Logger logger = LoggerFactory.getLogger(RestfulService.class);
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
            NetworkManager networkManager = host.getNetworkManager();
            ResourceResponse resourceResponse = networkManager.list(resourceRequest);
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

    @RequestMapping(value = "/{network}/{method}")
    public RestResponse<Object> handleNetwork(
            @PathVariable("network") String network, @PathVariable("method") String method) {
        //
        return null;
    }

    @RequestMapping(value = "/{network}/{stub}/{method}")
    public RestResponse<Object> handleStub(
            @PathVariable("network") String network,
            @PathVariable("stub") String stub,
            @PathVariable("method") String method) {
        // getState
        // getBlockHeader
        return null;
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
                case "getData":
                    {
                        if (resourceObj == null) {
                            throw new WeCrossException(
                                    ErrorCode.RESOURCE_ERROR, "Resource not found");
                        }
                        RestRequest<GetDataRequest> restRequest =
                                objectMapper.readValue(
                                        restRequestString,
                                        new TypeReference<RestRequest<GetDataRequest>>() {});

                        restRequest.checkRestRequest(path.toString(), method);

                        GetDataRequest getDataRequest = restRequest.getData();
                        GetDataResponse getDataResponse = resourceObj.getData(getDataRequest);

                        restResponse.setData(getDataResponse);
                        break;
                    }
                case "setData":
                    {
                        if (resourceObj == null) {
                            throw new WeCrossException(
                                    ErrorCode.RESOURCE_ERROR, "Resource not found");
                        }
                        RestRequest<SetDataRequest> restRequest =
                                objectMapper.readValue(
                                        restRequestString,
                                        new TypeReference<RestRequest<SetDataRequest>>() {});

                        restRequest.checkRestRequest(path.toString(), method);

                        SetDataRequest setDataRequest = (SetDataRequest) restRequest.getData();
                        SetDataResponse setDataResponse =
                                (SetDataResponse) resourceObj.setData(setDataRequest);

                        restResponse.setData(setDataResponse);
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
