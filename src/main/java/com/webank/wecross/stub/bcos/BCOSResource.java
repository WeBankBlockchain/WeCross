package com.webank.wecross.stub.bcos;

import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.web3j.protocol.Web3j;

import com.webank.wecross.bcp.EventCallback;
import com.webank.wecross.bcp.GetDataRequest;
import com.webank.wecross.bcp.GetDataResponse;
import com.webank.wecross.bcp.TransactionRequest;
import com.webank.wecross.bcp.Resource;
import com.webank.wecross.bcp.SetDataRequest;
import com.webank.wecross.bcp.SetDataResponse;
import com.webank.wecross.bcp.TransactionResponse;
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
