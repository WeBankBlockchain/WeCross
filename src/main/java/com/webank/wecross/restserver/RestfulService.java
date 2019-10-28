package com.webank.wecross.restserver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.network.NetworkManager;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.resource.request.GetDataRequest;
import com.webank.wecross.resource.request.ResourceRequest;
import com.webank.wecross.resource.request.SetDataRequest;
import com.webank.wecross.resource.request.TransactionRequest;
import com.webank.wecross.resource.response.GetDataResponse;
import com.webank.wecross.resource.response.ResourceResponse;
import com.webank.wecross.resource.response.SetDataResponse;
import com.webank.wecross.resource.response.TransactionResponse;
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
        restResponse.setVersion("0.1");
        restResponse.setResult(0);

        logger.debug("request string: {}", restRequestString);

        try {
            RestRequest<ResourceRequest> restRequest =
                    objectMapper.readValue(
                            restRequestString,
                            new TypeReference<RestRequest<ResourceRequest>>() {});

            ResourceRequest resourceRequest = restRequest.getData();
            NetworkManager networkManager = host.getNetworkManager();
            ResourceResponse resourceResponse = networkManager.list(resourceRequest);
            restResponse.setData(resourceResponse);
        } catch (Exception e) {
            logger.warn("Process request error:", e);

            restResponse.setResult(-1);
            restResponse.setMessage(e.getLocalizedMessage());
        }
        return restResponse;
    }

    @RequestMapping(value = "/state")
    public RestResponse<StateResponse> handlesState() {
        RestResponse<StateResponse> restResponse = new RestResponse<StateResponse>();

        StateResponse stateResponse = host.getState(new StateRequest());
        restResponse.setVersion("0.1");
        restResponse.setResult(0);
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
        restResponse.setVersion("0.1");
        restResponse.setResult(0);

        logger.debug("request string: {}", restRequestString);

        try {
            Resource resourceObj = host.getResource(path);
            if (resourceObj == null) {
                logger.warn("Unable to find resource: {}.{}.{}", network, stub, resource);

                throw new Exception("Resource not found");
            }

            switch (method) {
                case "exists":
                    {
                        restResponse.setData("exists!");
                        break;
                    }
                case "getData":
                    {
                        RestRequest<GetDataRequest> restRequest =
                                objectMapper.readValue(
                                        restRequestString,
                                        new TypeReference<RestRequest<GetDataRequest>>() {});

                        GetDataRequest getDataRequest = restRequest.getData();
                        GetDataResponse getDataResponse = resourceObj.getData(getDataRequest);

                        restResponse.setData(getDataResponse);
                        break;
                    }
                case "setData":
                    {
                        RestRequest<SetDataRequest> restRequest =
                                objectMapper.readValue(
                                        restRequestString,
                                        new TypeReference<RestRequest<SetDataRequest>>() {});

                        SetDataRequest setDataRequest = (SetDataRequest) restRequest.getData();
                        SetDataResponse setDataResponse =
                                (SetDataResponse) resourceObj.setData(setDataRequest);

                        restResponse.setData(setDataResponse);
                        break;
                    }
                case "call":
                    {
                        RestRequest<TransactionRequest> restRequest =
                                objectMapper.readValue(
                                        restRequestString,
                                        new TypeReference<RestRequest<TransactionRequest>>() {});

                        TransactionRequest transactionRequest =
                                (TransactionRequest) restRequest.getData();
                        TransactionResponse transactionResponse =
                                (TransactionResponse) resourceObj.call(transactionRequest);

                        restResponse.setData(transactionResponse);
                        break;
                    }
                case "sendTransaction":
                    {
                        RestRequest<TransactionRequest> restRequest =
                                objectMapper.readValue(
                                        restRequestString,
                                        new TypeReference<RestRequest<TransactionRequest>>() {});

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
                        restResponse.setResult(-1);
                        restResponse.setMessage("Unsupported method: " + method);
                        break;
                    }
            }
        } catch (Exception e) {
            logger.warn("Process request error:", e);

            restResponse.setResult(-1);
            restResponse.setMessage(e.getLocalizedMessage());
        }

        return restResponse;
    }
}
