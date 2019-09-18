package com.webank.wecross.bcp;

public interface Stub {
	public Resource getResource(String uri);
	public BlockHeader getBlockHeader(Integer number);
}
