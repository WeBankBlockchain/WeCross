package com.webank.wecross.bcp;

public interface Stub {
	public String getData(String table, String key);
	public void setData(String table, String key, String value);
	
	public Receipt sendTransaction(Transaction transaction);
	
	public BlockHeader getBlockHeader(Integer number);
}
