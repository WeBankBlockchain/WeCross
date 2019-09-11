package com.webank.wecross.bcp;

public interface Resource {
	public String getData(String key);
	public void setData(String key, String value);
	
	public Receipt sendTransaction(Transaction transaction);
	
	public Transaction newTransaction();
}
