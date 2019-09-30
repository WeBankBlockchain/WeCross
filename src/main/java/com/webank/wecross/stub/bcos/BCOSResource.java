package com.webank.wecross.stub.bcos;

import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.web3j.protocol.Web3j;

import com.webank.wecross.bcp.EventCallback;
import com.webank.wecross.bcp.Request;
import com.webank.wecross.bcp.Resource;
import com.webank.wecross.bcp.Response;
import com.webank.wecross.bcp.URI;

public class BCOSResource implements Resource {
	private Service bcosService;
	private Web3j web3;

	@Override
	public URI getURI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getData(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setData(String key, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public Response call(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response sendTransaction(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void registerEventHandler(EventCallback callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public Request createRequest() {
		// TODO Auto-generated method stub
		return null;
	}

	public Service getBcosService() {
		return bcosService;
	}

	public void setBcosService(Service bcosService) {
		this.bcosService = bcosService;
	}

	public Web3j getWeb3() {
		return web3;
	}

	public void setWeb3(Web3j web3) {
		this.web3 = web3;
	}

}
