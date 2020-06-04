package com.webank.wecross.stubmanager;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.zone.Chain;

public class MemoryBlockHeaderManager implements BlockHeaderManager {
    private Logger logger = LoggerFactory.getLogger(MemoryBlockHeaderManager.class);
    private ThreadPoolTaskExecutor threadPool;
    private Map<Long, List<GetBlockHeaderCallback>> getBlockHaderCallbacks = new HashMap<Long, List<GetBlockHeaderCallback> >();
    private LinkedList<BlockHeader> blockHeaderCache = new LinkedList<BlockHeader>();
    private Chain chain;
    private boolean running = false;
    private final int MAX_CACHE_SIZE = 10;
    
    public void onGetBlockNumber(long blockNumber) {
    	long current = 0;
    	if(!blockHeaderCache.isEmpty()) {
    		current = blockHeaderCache.peekLast().getNumber();
    	}
    	
    	if(current < blockNumber) {
    		if(current == 0) {
    			chain.getDriver().asyncGetBlockHeader(blockNumber, chain.chooseConnection(), new Driver.GetBlockHeaderCallback() {
					@Override
					public void onResponse(Exception e, BlockHeader blockHeader) {
						onGetBlockHeader(blockHeader, blockNumber);
					}
				});
    		}
    		else {
    			chain.getDriver().asyncGetBlockHeader(current + 1, chain.chooseConnection(), new Driver.GetBlockHeaderCallback() {
					@Override
					public void onResponse(Exception e, BlockHeader blockHeader) {
						onGetBlockHeader(blockHeader, blockNumber);
					}
				});
    		}
    	}
    	else {
    		chain.getDriver().asyncGetBlockNumber(chain.chooseConnection(), new Driver.GetBlockNumberCallback() {
				@Override
				public void onResponse(Exception e, long blockNumber) {
					onGetBlockNumber(blockNumber);
				}
			});
    	}
    }
    
    public void onGetBlockHeader(BlockHeader blockHeader, long target) {
    	blockHeaderCache.add(blockHeader);
    	List<GetBlockHeaderCallback> callbacks = getBlockHaderCallbacks.get(blockHeader.getNumber());
    	if(callbacks != null) {
    		for(GetBlockHeaderCallback callback: callbacks) {
    			threadPool.execute(new Runnable() {
					@Override
					public void run() {
						callback.onResponse(null, blockHeader);
					}
				});
    		}
    	}
    	getBlockHaderCallbacks.remove(blockHeader.getNumber());
    	
    	if(blockHeaderCache.size() > MAX_CACHE_SIZE) {
    		blockHeaderCache.pop();
    	}
    	
    	if(blockHeader.getNumber() < target) {
    		chain.getDriver().asyncGetBlockHeader(blockHeader.getNumber() + 1, chain.chooseConnection(), new Driver.GetBlockHeaderCallback() {
				@Override
				public void onResponse(Exception e, BlockHeader blockHeader) {
					onGetBlockHeader(blockHeader, target);
				}
			});
    	}
    	else {
    		chain.getDriver().asyncGetBlockNumber(chain.chooseConnection(), new Driver.GetBlockNumberCallback() {
				@Override
				public void onResponse(Exception e, long blockNumber) {
					onGetBlockNumber(blockNumber);
				}
			});
    	}
    }
    
    public void start() {
    	if(!running) {
    		running = true;
    		
    		chain.getDriver().asyncGetBlockNumber(chain.chooseConnection(), new Driver.GetBlockNumberCallback() {
				@Override
				public void onResponse(Exception e, long blockNumber) {
					onGetBlockNumber(blockNumber);
				}
			});
    	}
    }
    
	@Override
	public void asyncGetBlockNumber(GetBlockNumberCallback callback) {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				if(blockHeaderCache.isEmpty()) {
					callback.onResponse(null, 0);
				}
				else {
					callback.onResponse(null, blockHeaderCache.peekLast().getNumber());
				}
			}
		});
	}

    @Override
    public void asyncGetBlockHeader(long blockNumber, GetBlockHeaderCallback callback) {
    	if(blockHeaderCache.isEmpty() || blockHeaderCache.peekFirst().getNumber() < blockNumber) {
			chain.getDriver().asyncGetBlockHeader(blockNumber, chain.chooseConnection(), new Driver.GetBlockHeaderCallback() {
				@Override
				public void onResponse(Exception e, BlockHeader blockHeader) {
					callback.onResponse(e, blockHeader);
				}
			});
    	}
    	else if(blockNumber > blockHeaderCache.peekLast().getNumber()) {
    		if(!getBlockHaderCallbacks.containsKey(blockNumber)) {
    			getBlockHaderCallbacks.put(blockNumber, new LinkedList<GetBlockHeaderCallback>());
    		}
    	}
    	else {
    		for(BlockHeader blockHeader: blockHeaderCache) {
    			if(blockHeader.getNumber() == blockNumber) {
    				threadPool.execute(new Runnable() {
						@Override
						public void run() {
							callback.onResponse(null, blockHeader);
						}
					});
    				
    				break;
    			}
    		}
    	}
    }

	public ThreadPoolTaskExecutor getThreadPool() {
		return threadPool;
	}

	public void setThreadPool(ThreadPoolTaskExecutor threadPool) {
		this.threadPool = threadPool;
	}
}
