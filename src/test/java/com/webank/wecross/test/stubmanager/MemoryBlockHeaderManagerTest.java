package com.webank.wecross.test.stubmanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Driver.GetBlockHeaderCallback;
import com.webank.wecross.stub.Driver.GetBlockNumberCallback;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.Response;
import com.webank.wecross.stubmanager.MemoryBlockHeaderManager;
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

public class MemoryBlockHeaderManagerTest {
    private ObjectMapper objectMapper = new ObjectMapper();

    public byte[] buildBlockHeader(long number) {
        try {
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.setNumber(number);

            return objectMapper.writeValueAsBytes(blockHeader);
        } catch (Exception e) {
            Assert.assertNotNull(null);
        }

        return null;
    }

    @Test
    public void testSyncBlock() throws InterruptedException {
        MemoryBlockHeaderManager memoryBlockHeaderManager = new MemoryBlockHeaderManager();

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
                                    GetBlockNumberCallback callback = invocation.getArgument(1);
                                    threadPool.execute(
                                            () -> {
                                                callback.onResponse(null, 30);
                                            });

                                    return null;
                                })
                .when(driver)
                .asyncGetBlockNumber(Mockito.any(), Mockito.any());

        Mockito.when(driver.decodeBlockHeader(Mockito.any()))
                .thenAnswer(
                        (Answer<BlockHeader>)
                                invocation -> {
                                    byte[] data = invocation.getArgument(0);
                                    return objectMapper.readValue(data, BlockHeader.class);
                                });

        Mockito.doAnswer(
                        (Answer<Void>)
                                invocation -> {
                                    long blockNumber = invocation.getArgument(0);
                                    GetBlockHeaderCallback callback = invocation.getArgument(2);

                                    threadPool.execute(
                                            () -> {
                                                callback.onResponse(
                                                        null, buildBlockHeader(blockNumber));
                                            });

                                    return null;
                                })
                .when(driver)
                .asyncGetBlockHeader(Mockito.anyLong(), Mockito.any(), Mockito.any());

        Chain chain = Mockito.mock(Chain.class);
        Mockito.when(chain.getDriver()).thenReturn(driver);
        Mockito.when(chain.chooseConnection())
                .thenReturn(
                        new Connection() {
                            @Override
                            public Response send(Request request) {
                                return null;
                            }

                            @Override
                            public List<ResourceInfo> getResources() {
                                return null;
                            }

                            @Override
                            public Map<String, String> getProperties() {
                                return null;
                            }

                            @Override
                            public void setConnectionEventHandler(
                                    ConnectionEventHandler eventHandler) {}
                        });

        Timer timer = new HashedWheelTimer();

        memoryBlockHeaderManager.setThreadPool(threadPool);

        memoryBlockHeaderManager.asyncGetBlockNumber(
                (e, number) -> {
                    assertEquals(0, number);
                });

        memoryBlockHeaderManager.setChain(chain);
        memoryBlockHeaderManager.setMaxCacheSize(20);
        memoryBlockHeaderManager.setTimer(timer);
        memoryBlockHeaderManager.setGetBlockNumberDelay(100);

        memoryBlockHeaderManager.start();

        Thread.sleep(3000);

        memoryBlockHeaderManager.asyncGetBlockNumber(
                (e, number) -> {
                    assertNull(e);
                    assertEquals(30, number);
                });

        memoryBlockHeaderManager.asyncGetBlockHeader(
                21,
                (e, blockHeader) -> {
                    assertNull(e);
                    assertEquals(21, driver.decodeBlockHeader(blockHeader).getNumber());
                });

        Mockito.doAnswer(
                        (Answer<Void>)
                                invocation -> {
                                    GetBlockNumberCallback callback = invocation.getArgument(1);
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

        memoryBlockHeaderManager.asyncGetBlockNumber(
                (e, number) -> {
                    assertNull(e);
                    assertEquals(100, number);
                });

        Mockito.doAnswer(
                        (Answer<Void>)
                                invocation -> {
                                    long blockNumber = invocation.getArgument(0);
                                    GetBlockHeaderCallback callback = invocation.getArgument(2);

                                    assertTrue(blockNumber <= 80);

                                    threadPool.execute(
                                            () -> {
                                                callback.onResponse(
                                                        null, buildBlockHeader(blockNumber));
                                            });

                                    return null;
                                })
                .when(driver)
                .asyncGetBlockHeader(Mockito.anyLong(), Mockito.any(), Mockito.any());

        for (long i = 1; i < 100; ++i) {
            final long requestNumber = i;
            memoryBlockHeaderManager.asyncGetBlockHeader(
                    i,
                    (error, blockHeader) -> {
                        assertEquals(
                                requestNumber, driver.decodeBlockHeader(blockHeader).getNumber());
                    });
        }

        final List<Boolean> flags = new ArrayList<Boolean>();

        // test future block
        memoryBlockHeaderManager.asyncGetBlockHeader(
                150,
                (error, blockHeader) -> {
                    assertEquals(150, driver.decodeBlockHeader(blockHeader).getNumber());
                    flags.add(false);
                });

        assertTrue(flags.isEmpty());

        Mockito.doAnswer(
                        (Answer<Void>)
                                invocation -> {
                                    long blockNumber = invocation.getArgument(0);
                                    GetBlockHeaderCallback callback = invocation.getArgument(2);

                                    threadPool.execute(
                                            () -> {
                                                callback.onResponse(
                                                        null, buildBlockHeader(blockNumber));
                                            });

                                    return null;
                                })
                .when(driver)
                .asyncGetBlockHeader(Mockito.anyLong(), Mockito.any(), Mockito.any());

        Mockito.doAnswer(
                        (Answer<Void>)
                                invocation -> {
                                    GetBlockNumberCallback callback = invocation.getArgument(1);
                                    threadPool.execute(
                                            () -> {
                                                callback.onResponse(null, 200);
                                            });

                                    return null;
                                })
                .when(driver)
                .asyncGetBlockNumber(Mockito.any(), Mockito.any());

        Thread.sleep(3000);

        assertFalse(flags.isEmpty());

        memoryBlockHeaderManager.asyncGetBlockHeader(
                500,
                (error, blockHeader) -> {
                    assertNotNull(error);
                    assertEquals("Operation canceled", error.getMessage());
                });

        Thread.sleep(10000); // Sleep for finish
        memoryBlockHeaderManager.stop();
    }
}
