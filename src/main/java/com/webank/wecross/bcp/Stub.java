package com.webank.wecross.bcp;

public interface Stub {
	public void init() throws Exception;
	public String getPattern();
	public Resource getResource(URI uri) throws Exception;
}
