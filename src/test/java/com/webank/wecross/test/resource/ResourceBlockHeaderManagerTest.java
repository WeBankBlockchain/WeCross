package com.webank.wecross.test.resource;

import com.webank.wecross.config.ResourceThreadPoolConfig;
import com.webank.wecross.resource.ResourceBlockHeaderManager;
import com.webank.wecross.resource.ResourceBlockHeaderManagerFactory;
import com.webank.wecross.storage.BlockHeaderStorage;
import com.webank.wecross.storage.RocksDBBlockHeaderStorageFactory;
import com.webank.wecross.stub.BlockHeaderManager;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Test;

public class ResourceBlockHeaderManagerTest {
    @Test
    public void getBlockHeaderTest() {
        ResourceThreadPoolConfig.ResourceThreadPool resourceThreadPool =
                new ResourceThreadPoolConfig.ResourceThreadPool(10, 10, 200);

        ResourceBlockHeaderManagerFactory resourceBlockHeaderManagerFactory =
                new ResourceBlockHeaderManagerFactory(resourceThreadPool);
        RocksDBBlockHeaderStorageFactory rocksDBBlockHeaderStorageFactory =
                new RocksDBBlockHeaderStorageFactory();
        BlockHeaderStorage blockHeaderStorage =
                rocksDBBlockHeaderStorageFactory.newBlockHeaderStorage(
                        "unittest" + System.currentTimeMillis());
        ResourceBlockHeaderManager blockHeaderManager =
                resourceBlockHeaderManagerFactory.build(blockHeaderStorage);

        // Test timeout
        Assert.assertTrue(blockHeaderManager.getBlockHeader(0) == null);

        // Test get api
        byte[] blockHeader = mockBlockHeader(0);
        blockHeaderStorage.writeBlockHeader(0, blockHeader);

        Assert.assertTrue(Arrays.equals(blockHeader, blockHeaderManager.getBlockHeader(0)));
    }

    @Test
    public void asyncGetBlockHeaderTest() throws Exception {
        ResourceThreadPoolConfig.ResourceThreadPool resourceThreadPool =
                new ResourceThreadPoolConfig.ResourceThreadPool(10, 10, 200);

        ResourceBlockHeaderManagerFactory resourceBlockHeaderManagerFactory =
                new ResourceBlockHeaderManagerFactory(resourceThreadPool);
        RocksDBBlockHeaderStorageFactory rocksDBBlockHeaderStorageFactory =
                new RocksDBBlockHeaderStorageFactory();
        BlockHeaderStorage blockHeaderStorage =
                rocksDBBlockHeaderStorageFactory.newBlockHeaderStorage(
                        "unittest" + System.currentTimeMillis());
        ResourceBlockHeaderManager blockHeaderManager =
                resourceBlockHeaderManagerFactory.build(blockHeaderStorage);

        // Test timeout
        CompletableFuture<byte[]> timeoutFuture = new CompletableFuture<>();
        blockHeaderManager.asyncGetBlockHeader(
                0,
                new BlockHeaderManager.BlockHeaderCallback() {
                    @Override
                    public void onBlockHeader(byte[] blockHeader) {
                        timeoutFuture.complete(blockHeader);
                    }
                });

        Assert.assertEquals(null, timeoutFuture.get());

        // Callback queue test
        Map<Long, byte[]> blockHeaderMap = new HashMap<>();
        int maxBlockCnt = 10;
        for (int i = maxBlockCnt - 1; i >= 0; i--) {
            long blockNumber = i;
            blockHeaderManager.asyncGetBlockHeader(
                    i,
                    new BlockHeaderManager.BlockHeaderCallback() {
                        @Override
                        public void onBlockHeader(byte[] blockHeader) {
                            byte[] cmp = mockBlockHeader(blockNumber);
                            Assert.assertTrue(Arrays.equals(blockHeader, cmp));
                            blockHeaderMap.put(blockNumber, blockHeader);
                        }
                    });
        }

        for (int i = 0; i < maxBlockCnt; i++) {
            blockHeaderStorage.writeBlockHeader(i, mockBlockHeader(i));
            Thread.sleep(100);
            Assert.assertEquals(blockHeaderMap.size(), i + 1);
        }
    }

    private byte[] mockBlockHeader(long blockNumber) {
        String str = String.valueOf(blockNumber);
        return str.getBytes();
    }
}
