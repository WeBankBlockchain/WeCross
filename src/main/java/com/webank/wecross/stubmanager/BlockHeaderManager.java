package com.webank.wecross.stubmanager;

import com.webank.wecross.stub.BlockHeader;

public interface BlockHeaderManager {
	public void start();
	
	public void stop();
	
	public interface GetBlockNumberCallback {
		void onResponse(Exception e, long blockNumber);
	}
	
    public void asyncGetBlockNumber(GetBlockNumberCallback callback);

    public interface GetBlockHeaderCallback {
        void onResponse(Exception e, BlockHeader blockHeader);
    }
    
    void asyncGetBlockHeader(long blockNumber, BlockHeaderManager.GetBlockHeaderCallback callback);
}
