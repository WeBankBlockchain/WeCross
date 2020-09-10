package com.webank.wecross.test.stubmanager;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.Block;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.BlockManager;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stubmanager.MemoryBlockManager;
import com.webank.wecross.zone.Chain;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class MemoryBlockManagerTest {
    private ObjectMapper objectMapper = new ObjectMapper();

    public BlockHeader buildBlockHeader(long number) {
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setNumber(number);
        return blockHeader;
    }

    public Block buildBlock(long number) {
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setNumber(number);
        Block block = new Block();
        block.setBlockHeader(blockHeader);
        block.setRawBytes(new byte[] {});
        return block;
    }

    @Test
    public void testSyncBlock() throws InterruptedException {
        MemoryBlockManager memoryBlockManager = new MemoryBlockManager();

        ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();
        threadPool.setCorePoolSize(10);
        threadPool.setMaxPoolSize(100);
        threadPool.setQueueCapacity(1000);
        threadPool.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        threadPool.initialize();

        Driver driver = Mockito.mock(Driver.class);
        Mockito.doAnswer(
                        (Answer<Void>)
                                invocation -> {
                                    Driver.GetBlockNumberCallback callback =
                                            invocation.getArgument(1);
                                    threadPool.execute(
                                            () -> {
                                                callback.onResponse(null, 30);
                                            });

                                    return null;
                                })
                .when(driver)
                .asyncGetBlockNumber(Mockito.any(), Mockito.any());

        Mockito.doAnswer(
                        (Answer<Void>)
                                invocation -> {
                                    long blockNumber = invocation.getArgument(0);
                                    Driver.GetBlockCallback callback = invocation.getArgument(2);

                                    threadPool.execute(
                                            () -> {
                                                callback.onResponse(null, buildBlock(blockNumber));
                                            });

                                    return null;
                                })
                .when(driver)
                .asyncGetBlock(
                        Mockito.anyLong(), Mockito.anyBoolean(), Mockito.any(), Mockito.any());

        Chain chain = Mockito.mock(Chain.class);
        Mockito.when(chain.getDriver()).thenReturn(driver);
        Mockito.when(chain.chooseConnection())
                .thenReturn(
                        new Connection() {
                            @Override
                            public void asyncSend(Request request, Callback callback) {}

                            @Override
                            public void setConnectionEventHandler(
                                    ConnectionEventHandler eventHandler) {}

                            @Override
                            public Map<String, String> getProperties() {
                                return null;
                            }
                        });

        Timer timer = new HashedWheelTimer();

        memoryBlockManager.setThreadPool(threadPool);

        memoryBlockManager.asyncGetBlockNumber(
                (e, number) -> {
                    assertEquals(0, number);
                });

        memoryBlockManager.setChain(chain);
        memoryBlockManager.setMaxCacheSize(20);
        memoryBlockManager.setTimer(timer);
        memoryBlockManager.setGetBlockNumberDelay(100);

        memoryBlockManager.start();

        Thread.sleep(3000);

        memoryBlockManager.asyncGetBlockNumber(
                (e, number) -> {
                    assertNull(e);
                    assertEquals(30, number);
                });

        Mockito.doAnswer(
                        (Answer<Void>)
                                invocation -> {
                                    Driver.GetBlockNumberCallback callback =
                                            invocation.getArgument(1);
                                    threadPool.execute(
                                            () -> {
                                                callback.onResponse(null, 100);
                                            });

                                    return null;
                                })
                .when(driver)
                .asyncGetBlockNumber(Mockito.any(), Mockito.any());

        Thread.sleep(3000);

        // Test sync block

        memoryBlockManager.asyncGetBlockNumber(
                (e, number) -> {
                    assertNull(e);
                    assertEquals(100, number);
                });

        Mockito.doAnswer(
                        (Answer<Void>)
                                invocation -> {
                                    long blockNumber = invocation.getArgument(0);
                                    BlockManager.GetBlockCallback callback =
                                            invocation.getArgument(2);

                                    assertTrue(blockNumber <= 80);

                                    threadPool.execute(
                                            () -> {
                                                callback.onResponse(null, buildBlock(blockNumber));
                                            });

                                    return null;
                                })
                .when(driver)
                .asyncGetBlock(
                        Mockito.anyLong(), Mockito.anyBoolean(), Mockito.any(), Mockito.any());

        final List<Boolean> flags = new ArrayList<Boolean>();

        assertTrue(flags.isEmpty());

        Mockito.doAnswer(
                        (Answer<Void>)
                                invocation -> {
                                    long blockNumber = invocation.getArgument(0);
                                    Driver.GetBlockCallback callback = invocation.getArgument(3);

                                    threadPool.execute(
                                            () -> {
                                                callback.onResponse(null, buildBlock(blockNumber));
                                            });

                                    return null;
                                })
                .when(driver)
                .asyncGetBlock(
                        Mockito.anyLong(), Mockito.anyBoolean(), Mockito.any(), Mockito.any());

        Mockito.doAnswer(
                        (Answer<Void>)
                                invocation -> {
                                    Driver.GetBlockNumberCallback callback =
                                            invocation.getArgument(1);
                                    threadPool.execute(
                                            () -> {
                                                callback.onResponse(null, 200);
                                            });

                                    return null;
                                })
                .when(driver)
                .asyncGetBlockNumber(Mockito.any(), Mockito.any());

        Thread.sleep(10000);

        // assertFalse(flags.isEmpty());

        memoryBlockManager.asyncGetBlock(
                500,
                (error, blockHeader) -> {
                    assertNotNull(error);
                    assertEquals("Operation canceled", error.getMessage());
                });

        waitingForAllDone(threadPool, "last");
        memoryBlockManager.stop();
    }

    private void waitingForAllDone(ThreadPoolTaskExecutor threadPool, String prefix)
            throws InterruptedException {
        int waitingTimes = 0;
        long taskNum;
        do {
            taskNum = threadPool.getThreadPoolExecutor().getQueue().size();
            waitingTimes++;
            System.out.println(prefix + " waiting[" + waitingTimes + "], taskNum: " + taskNum);
            Thread.sleep(1000);
            Assert.assertTrue(waitingTimes < 60); // 1 min
        } while (taskNum > 0);
    }
}
