package com.webank.wecross.test;

import com.webank.wecross.bcp.Event;
import com.webank.wecross.bcp.EventCallback;
import com.webank.wecross.bcp.Response;
import com.webank.wecross.bcp.Resource;
import com.webank.wecross.bcp.Stub;
import com.webank.wecross.bcp.Request;

public class StubTest {
	public void Test() {
		Stub stub = null;
		Resource contractResource = stub.getResource("bcp://payment.bcos.HelloWorldContract");

		//call the get method
		Request getTransaction = contractResource.createRequest();
		getTransaction.setFrom("my_account");
		getTransaction.setMethod("get");
		
		Response getReceipt = contractResource.call(getTransaction);
		
		//send transaction to set method
		Request setTransaction = contractResource.createRequest();
		setTransaction.setFrom("my_account");
		setTransaction.setMethod("set");
		setTransaction.setArgs(new Object[]{"world"});
		
		Response setReceipt = contractResource.sendTransaction(setTransaction);
		
		Resource amopServerResource = stub.getResource("bcp://payment.bcos.PaymentNotify");
		amopServerResource.registerEventHandler(new EventCallback() {
			@Override
			public void onEvent(Event event) {
				System.out.println(event.getContent()[0]);
			}
		});
		
		Resource amopClientResource = stub.getResource("bcp://payment.bcos.PaymentNotify2");
		Request amopTransaction = contractResource.createRequest();
		amopTransaction.setMethod("sendMessage");
		amopTransaction.setArgs(new Object[] {"Hello world!"});
		
		Response amopReceipt = amopClientResource.call(amopTransaction);
	}
}
