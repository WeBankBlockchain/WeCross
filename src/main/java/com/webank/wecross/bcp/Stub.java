package com.webank.wecross.bcp;

public interface Stub {
	public Resource getResource(URI uri);
	public BlockHeader getBlockHeader(Integer number);
}
