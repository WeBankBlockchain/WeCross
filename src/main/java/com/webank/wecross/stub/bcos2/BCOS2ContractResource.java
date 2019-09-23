package com.webank.wecross.stub.bcos2;

import org.fisco.bcos.channel.client.Service;

import com.webank.wecross.bcp.EventCallback;
import com.webank.wecross.bcp.Response;
import com.webank.wecross.bcp.Resource;
import com.webank.wecross.bcp.Request;
import com.webank.wecross.bcp.URI;

public class BCOS2ContractResource implements Resource {
	private Service bcos2Service;
	
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
	public Request newTransaction() {
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

}
