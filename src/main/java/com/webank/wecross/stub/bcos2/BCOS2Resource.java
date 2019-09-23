package com.webank.wecross.stub.bcos2;

import org.fisco.bcos.channel.client.Service;

import com.webank.wecross.bcp.EventCallback;
import com.webank.wecross.bcp.Receipt;
import com.webank.wecross.bcp.Resource;
import com.webank.wecross.bcp.Transaction;

public class BCOS2Resource implements Resource {
	private Service bcos2Service;
	
	@Override
	public String getData(String key) {
		return null;
	}

	@Override
	public void setData(String key, String value) {
		
	}

	@Override
	public Receipt sendTransaction(Transaction transaction) {
		return null;
	}

	@Override
	public Transaction newTransaction() {
		return null;
	}

	@Override
	public Receipt call(Transaction transaction) {
		return null;
	}

	@Override
	public void registerEventHandler(EventCallback callback) {
	}

}
