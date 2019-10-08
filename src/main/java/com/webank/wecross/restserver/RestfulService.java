package com.webank.wecross.restserver;

import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.bcp.GetDataRequest;
import com.webank.wecross.bcp.GetDataResponse;
import com.webank.wecross.bcp.Resource;
import com.webank.wecross.bcp.SetDataRequest;
import com.webank.wecross.bcp.SetDataResponse;
import com.webank.wecross.bcp.TransactionRequest;
import com.webank.wecross.bcp.TransactionResponse;
import com.webank.wecross.bcp.URI;
import com.webank.wecross.core.StubManager;

@RestController
@SpringBootApplication
public class RestfulService {
	private StubManager stubManager;
	private Logger logger = LoggerFactory.getLogger(RestfulService.class);
	private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
	
	public static void main(String[] args) {
		SpringApplication.run(RestfulService.class, args);
	}
	
	@RequestMapping("/test")
	public String test() {
		return "OK!";
	}

	@RequestMapping(value = "/resource/{network}/{chain}/{resource}", method = RequestMethod.POST)
	public RestResponse<Object> handleResource(@PathVariable("network") String network,
			@PathVariable("chain") String chain, @PathVariable("resource") String resource,
			@RequestParam("method") String method, @RequestBody String restRequestString) {
		URI uri = new URI();
		uri.setNetwork(network);
		uri.setChain(chain);
		uri.setResource(resource);
		
		RestResponse<Object> restResponse = new RestResponse<Object>();
		restResponse.setVersion("0.1");
		restResponse.setResult(0);

		try {
			Resource resourceObj = stubManager.getResource(uri);
			if (resourceObj == null) {
				logger.warn("Unable to find resource: {}.{}.{}", network, chain, resource);
			}
		
			switch (method) {
			case "getData": {
				RestRequest<GetDataRequest> restRequest = objectMapper.readValue(restRequestString,
						new TypeReference<RestRequest<GetDataRequest>>() {
						});

				GetDataRequest getDataRequest = restRequest.getData();
				GetDataResponse getDataResponse = resourceObj.getData(getDataRequest);

				restResponse.setData(getDataResponse);
				break;
			}
			case "setData": {
				RestRequest<SetDataRequest> restRequest = objectMapper.readValue(restRequestString,
						new TypeReference<RestRequest<SetDataRequest>>() {
						});

				SetDataRequest setDataRequest = (SetDataRequest) restRequest.getData();
				SetDataResponse setDataResponse = (SetDataResponse) resourceObj.setData(setDataRequest);

				restResponse.setData(setDataResponse);
				break;
			}
			case "call": {
				RestRequest<TransactionRequest> restRequest = objectMapper.readValue(restRequestString,
						new TypeReference<RestRequest<TransactionRequest>>() {
						});

				TransactionRequest transactionRequest = (TransactionRequest) restRequest.getData();
				TransactionResponse transactionResponse = (TransactionResponse) resourceObj.call(transactionRequest);

				restResponse.setData(transactionResponse);
				break;
			}
			case "sendTransaction": {
				RestRequest<TransactionRequest> restRequest = objectMapper.readValue(restRequestString,
						new TypeReference<RestRequest<TransactionRequest>>() {
						});

				TransactionRequest transactionRequest = (TransactionRequest) restRequest.getData();
				TransactionResponse transactionResponse = (TransactionResponse) resourceObj
						.sendTransaction(transactionRequest);

				restResponse.setData(transactionResponse);
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

	public StubManager getStubManager() {
		return stubManager;
	}

	public void setStubManager(StubManager stubManager) {
		this.stubManager = stubManager;
	}
}
