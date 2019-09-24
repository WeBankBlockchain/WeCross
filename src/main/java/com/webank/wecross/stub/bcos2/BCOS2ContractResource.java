package com.webank.wecross.stub.bcos2;

import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.web3j.protocol.Web3j;

import com.webank.wecross.bcp.EventCallback;
import com.webank.wecross.bcp.Response;
import com.webank.wecross.bcp.Resource;
import com.webank.wecross.bcp.Request;
import com.webank.wecross.bcp.URI;

public class BCOS2ContractResource implements Resource {
	private URI uri;
	private Service bcos2Service;
	private Web3j web3;
	
	@Override
	public String getData(String key) {
		return null;
	}

	@Override
	public void setData(String key, String value) {
		
	}

	@Override
	public Response sendTransaction(Request request) {
		return null;
	}

	@Override
	public Request createRequest() {
		return new BCOS2Request();
	}

	@Override
	public Response call(Request request) {
		return null;
	}

	@Override
	public void registerEventHandler(EventCallback callback) {
	}

	@Override
	public URI getURI() {
		return uri;
	}
	
	public Service getBcos2Service() {
		return bcos2Service;
	}

	public void setBcos2Service(Service bcos2Service) {
		this.bcos2Service = bcos2Service;
	}

	public Web3j getWeb3() {
		return web3;
	}

	public void setWeb3(Web3j web3) {
		this.web3 = web3;
	}
}
