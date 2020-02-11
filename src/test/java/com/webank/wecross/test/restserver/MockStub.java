package com.webank.wecross.test.restserver;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.Stub;

public class MockStub implements Stub {
	@Override
	public String getType() {
		return "test-stub";
	}

	@Override
	public int getBlockNumber() {
		return 666;
	}

	@Override
	public BlockHeader getBlockHeader(int blockNumber) {
		BlockHeader blockHeader = new BlockHeader();
		blockHeader.setNumber(blockNumber);
		return blockHeader;
	}

	@Override
	public Map<String, Resource> getResources() {
		Map<String, Resource> resources = new HashMap<String, Resource>();
		return resources;
	}

	@Override
	public Logger getLogger() {
		// TODO Auto-generated method stub
		return null;
	}

}
