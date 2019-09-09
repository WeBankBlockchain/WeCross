package com.webank.wecross.bcp;

public interface Stub {
	public Data getData(String table, String key);
	public void setData(String table, String key, Data data);
	
	public Receipt sendTransaction(Transaction transaction);
}
