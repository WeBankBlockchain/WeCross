package com.webank.wecross.test;

import com.webank.wecross.bcp.Event;
import com.webank.wecross.bcp.EventCallback;
import com.webank.wecross.bcp.Receipt;
import com.webank.wecross.bcp.Resource;
import com.webank.wecross.bcp.Stub;
import com.webank.wecross.bcp.Transaction;

public class StubTest {
	public void Test() {
		Stub stub = null;
		Resource contractResource = stub.getResource("bcp://payment.bcos.HelloWorldContract");

		//call the get method
		Transaction getTransaction = contractResource.newTransaction();
		getTransaction.setFrom("my_account");
		getTransaction.setMethod("get");
		
		Receipt getReceipt = contractResource.call(getTransaction);
		
		//send transaction to set method
		Transaction setTransaction = contractResource.newTransaction();
		setTransaction.setFrom("my_account");
		setTransaction.setMethod("set");
		setTransaction.setArgs(new Object[]{"world"});
		
		Receipt setReceipt = contractResource.sendTransaction(setTransaction);
		
		Resource amopServerResource = stub.getResource("bcp://payment.bcos.PaymentNotify");
		amopServerResource.registerEventHandler(new EventCallback() {
			@Override
			public void onEvent(Event event) {
				System.out.println(event.getContent()[0]);
			}
		});
		
		Resource amopClientResource = stub.getResource("bcp://payment.bcos.PaymentNotify2");
		Transaction amopTransaction = contractResource.newTransaction();
		amopTransaction.setMethod("sendMessage");
		amopTransaction.setArgs(new Object[] {"Hello world!"});
		
		Receipt amopReceipt = amopClientResource.call(amopTransaction);
	}
}
