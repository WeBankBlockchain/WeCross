package com.webank.wecross.test.stubmanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.concurrent.ThreadPoolExecutor;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.webank.wecross.zone.Chain;

import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Driver.GetBlockHeaderCallback;
import com.webank.wecross.stub.Driver.GetBlockNumberCallback;
import com.webank.wecross.stubmanager.MemoryBlockHeaderManager;

public class MemoryBlockHeaderManagerTest {
	@Test
	public void testSyncBlock() throws InterruptedException {
		MemoryBlockHeaderManager memoryBlockHeaderManager = new MemoryBlockHeaderManager();
		
		/*
		ThreadPoolTaskExecutor threadPool = Mockito.mock(ThreadPoolTaskExecutor.class);
		Mockito.doAnswer((Answer<Void>) invocation -> {
			Runnable runnable = invocation.getArgument(0);
			runnable.run();
			
			return null;
		}).when(threadPool).execute(Mockito.any());
		*/
		
		ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();
		threadPool.setCorePoolSize(1);
		threadPool.setMaxPoolSize(1);
		threadPool.setQueueCapacity(2);
		threadPool.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
		threadPool.initialize();
		
		Driver driver = Mockito.mock(Driver.class);
		Mockito.doAnswer((Answer<Void>) invocation -> {
			GetBlockNumberCallback callback = invocation.getArgument(1);
			threadPool.execute(() -> {
				callback.onResponse(null, 30);
			});
			
			return null;
		}).when(driver).asyncGetBlockNumber(Mockito.any(), Mockito.any());
		
		Mockito.doAnswer((Answer<Void>) invocation -> {
			long blockNumber = invocation.getArgument(0);
			GetBlockHeaderCallback callback = invocation.getArgument(2);
			
			BlockHeader blockHeader = new BlockHeader();
			blockHeader.setNumber(blockNumber);
			
			threadPool.execute(() -> {
				callback.onResponse(null, blockHeader);
			});
			
			return null;
		}).when(driver).asyncGetBlockHeader(Mockito.anyLong(), Mockito.any(), Mockito.any());
		
		Chain chain = Mockito.mock(Chain.class);
		Mockito.when(chain.getDriver()).thenReturn(driver);
		
		memoryBlockHeaderManager.setThreadPool(threadPool);
		memoryBlockHeaderManager.setChain(chain);
		memoryBlockHeaderManager.setMaxCacheSize(20);
		
		memoryBlockHeaderManager.start();
		
		Thread.sleep(300);
		
		memoryBlockHeaderManager.asyncGetBlockNumber((e, number) -> {
			assertNull(e);
			assertEquals(30, number);
		});
		
		memoryBlockHeaderManager.asyncGetBlockHeader(21, (e, blockHeader) -> {
			assertNull(e);
			assertEquals(21, blockHeader.getNumber());
		});
	}
}
