package com.webank.wecross.test.stub;

public class StubTest {
    /*
    public void Test() {
    	Stub stub = null;
    	Resource contractResource = stub.getResource("bcp://payment.bcos.HelloWorldContract");

    	//call the get method
    	TransactionRequest getTransaction = contractResource.createRequest();
    	getTransaction.setFrom("my_account");
    	getTransaction.setMethod("get");

    	TransactionResponse getReceipt = contractResource.call(getTransaction);

    	//send transaction to set method
    	TransactionRequest setTransaction = contractResource.createRequest();
    	setTransaction.setFrom("my_account");
    	setTransaction.setMethod("set");
    	setTransaction.setArgs(new Object[]{"world"});

    	TransactionResponse setReceipt = contractResource.sendTransaction(setTransaction);

    	Resource amopServerResource = stub.getResource("bcp://payment.bcos.PaymentNotify");
    	amopServerResource.registerEventHandler(new EventCallback() {
    		@Override
    		public void onEvent(Event event) {
    			System.out.println(event.getContent()[0]);
    		}
    	});

    	Resource amopClientResource = stub.getResource("bcp://payment.bcos.PaymentNotify2");
    	TransactionRequest amopTransaction = contractResource.createRequest();
    	amopTransaction.setMethod("sendMessage");
    	amopTransaction.setArgs(new Object[] {"Hello world!"});

    	TransactionResponse amopReceipt = amopClientResource.call(amopTransaction);
    }
    */
}
