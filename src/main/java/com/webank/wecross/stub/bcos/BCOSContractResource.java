package com.webank.wecross.stub.bcos;

import com.webank.wecross.bcp.EventCallback;
import com.webank.wecross.bcp.Response;

import com.webank.wecross.bcp.Request;

import java.io.IOException;

import org.fisco.bcos.channel.client.CallContract;
import org.fisco.bcos.web3j.abi.datatypes.Type;

public class BCOSContractResource extends BCOSResource {
	private String contractAddress;
	private CallContract callContract;
	
	@Override
	public String getData(String key) {
		return null;
	}

	@Override
	public void setData(String key, String value) {
		
	}

	@Override
	public Response sendTransaction(Request request) {
		BCOSResponse bcosResponse = new BCOSResponse();
		
		String result = callContract.sendTransaction(contractAddress, request.getMethod(), (Type[]) request.getArgs());
		
		if(result.isEmpty()) {
			bcosResponse.setErrorCode(1);
			bcosResponse.setErrorMessage("Result is empty, please check contract address and arguments");
		}
		else {
			bcosResponse.setErrorCode(0);
			bcosResponse.setErrorMessage("");
			bcosResponse.setResult(new Object[] {result});
		}
		
		return bcosResponse;
	}

	@Override
	public Request createRequest() {
		return new BCOSRequest();
	}

	@Override
	public Response call(Request request) {
		BCOSResponse bcosResponse = new BCOSResponse();
		
		try {
			String result = callContract.call(contractAddress, request.getMethod(), (Type[]) request.getArgs());
			
			if(result.isEmpty()) {
				bcosResponse.setErrorCode(1);
				bcosResponse.setErrorMessage("Result is empty, please check contract address and arguments");
			}
			else {
				bcosResponse.setErrorCode(0);
				bcosResponse.setErrorMessage("");
				bcosResponse.setResult(new Object[] {result});
			}
			
			return bcosResponse;
		} catch (IOException e) {
			bcosResponse.setErrorCode(2);
			bcosResponse.setErrorMessage("Unexpected error: " + e.getMessage());
			
			return bcosResponse;
		}
	}

	@Override
	public void registerEventHandler(EventCallback callback) {
	}
}
