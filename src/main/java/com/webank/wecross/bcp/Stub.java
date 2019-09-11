package com.webank.wecross.bcp;

public interface Stub {
	public Resource getResource(String path);
	
	public BlockHeader getBlockHeader(Integer number);
}
