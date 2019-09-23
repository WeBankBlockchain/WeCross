package com.webank.wecross.bcp;

public interface Resource {
	public URI getURI();
	public String getData(String key);
	public void setData(String key, String value);
	
	public Receipt call(Transaction transaction);
	public Receipt sendTransaction(Transaction transaction);
	
	public void registerEventHandler(EventCallback callback);
	
	public Transaction newTransaction();
}
