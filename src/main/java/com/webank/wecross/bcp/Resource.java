package com.webank.wecross.bcp;

public interface Resource {
	public URI getURI();
	public String getData(String key);
	public void setData(String key, String value);
	
	public Response call(Request request);
	public Response sendTransaction(Request request);
	
	public void registerEventHandler(EventCallback callback);
	
	public Request newTransaction();
}
