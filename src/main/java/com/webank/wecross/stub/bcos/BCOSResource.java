package com.webank.wecross.stub.bcos;

import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;

import com.webank.wecross.resource.EventCallback;
import com.webank.wecross.resource.GetDataRequest;
import com.webank.wecross.resource.GetDataResponse;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.resource.SetDataRequest;
import com.webank.wecross.resource.SetDataResponse;
import com.webank.wecross.resource.TransactionRequest;
import com.webank.wecross.resource.TransactionResponse;
import com.webank.wecross.resource.URI;

public class BCOSResource implements Resource {
	private Service bcosService;
	private Web3j web3;
	
	public void init(Service service, Web3j web3j, Credentials credentials) {
		
	}

	@Override
	public URI getURI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GetDataResponse getData(GetDataRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SetDataResponse setData(SetDataRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TransactionResponse call(TransactionRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TransactionResponse sendTransaction(TransactionRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void registerEventHandler(EventCallback callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public TransactionRequest createRequest() {
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
