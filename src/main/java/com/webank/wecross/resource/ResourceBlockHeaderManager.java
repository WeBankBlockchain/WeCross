package com.webank.wecross.resource;

import com.webank.wecross.storage.BlockHeaderStorage;
import com.webank.wecross.stub.BlockHeaderManager;
import com.webank.wecross.zone.Chain;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class ResourceBlockHeaderManager implements BlockHeaderManager {
    private Logger logger = LoggerFactory.getLogger(ResourceBlockHeaderManager.class);
    private static final long callbackTimeout = 20000; // ms
    private Queue<Runnable> blockHeaderCallbackTasks = new ConcurrentLinkedQueue<>();
    private BlockHeaderStorage blockHeaderStorage;
    private Chain chain;
    private ThreadPoolTaskExecutor threadPool;
    private Timer clearCallbackTimer;

    class BlockHeaderCache {
        private long blockNumber;
        private byte[] blockHeader;

        BlockHeaderCache(long blockNumber, byte[] blockHeader) {
            this.blockNumber = blockNumber;
            this.blockHeader = blockHeader;
        }

        public long getBlockNumber() {
            return blockNumber;
        }

        public byte[] getBlockHeader() {
            return blockHeader;
        }
    }

    private BlockHeaderCache blockHeaderCache = new BlockHeaderCache(-1, null);

    public ResourceBlockHeaderManager() {

        clearCallbackTimer = new Timer("clearCallbackTimer");
        clearCallbackTimer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        clearTimeoutCallback();
                    }
                },
                callbackTimeout,
                callbackTimeout);
    }

    @Override
    public long getBlockNumber() {
        return blockHeaderStorage.readBlockNumber();
    }

    @Override
    public byte[] getBlockHeader(long blockNumber) {
        byte[] data = null;
        CompletableFuture<byte[]> future = new CompletableFuture<>();
        asyncGetBlockHeader(
                blockNumber,
                new BlockHeaderCallback() {
                    @Override
                    public void onBlockHeader(byte[] blockHeader) {
                        if (!future.isCancelled()) {
                            future.complete(blockHeader);
                        }
                    }
                });

        try {
            return future.get(callbackTimeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.warn("getBlockHeader failed, blockNumber:{}, exception:{}", blockNumber, e);
            future.cancel(true);
            return null;
        }
    }

    @Override
    public void asyncGetBlockHeader(long blockNumber, BlockHeaderCallback callback) {
        chain.noteBlockHeaderChange();

        long timeout = callbackTimeout + System.currentTimeMillis();
        asyncGetBlockHeaderInternal(blockNumber, callback, timeout);
    }

    private void asyncGetBlockHeaderInternal(
            long blockNumber, BlockHeaderCallback callback, long timeout) {
        BlockHeaderCache cache = this.blockHeaderCache;
        if (cache.getBlockNumber() == blockNumber) {
            callback.onBlockHeader(cache.blockHeader);
        } else if (cache.getBlockNumber() > blockNumber) {
            callback.onBlockHeader(this.blockHeaderStorage.readBlockHeader(blockNumber));
        } else if (System.currentTimeMillis() > timeout) {
            logger.warn("asyncGetBlockHeader timeout, number:" + blockNumber);
            callback.onBlockHeader(null);
        } else {
            // async request
            synchronized (this) {
                getBlockHeaderCallbackTasks()
                        .add(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        asyncGetBlockHeaderInternal(blockNumber, callback, timeout);
                                    }
                                });
            }
        }
    }

    public BlockHeaderStorage getBlockHeaderStorage() {
        return blockHeaderStorage;
    }

    public void setBlockHeaderStorage(BlockHeaderStorage blockHeaderStorage) {
        this.blockHeaderStorage = blockHeaderStorage;
        if (blockHeaderStorage != null) {
            this.blockHeaderStorage.registerOnBlockHeader(
                    new BiConsumer<Long, byte[]>() {
                        @Override
                        public void accept(Long blockNumber, byte[] blockHeader) {
                            onBlockHeader(blockNumber.longValue(), blockHeader);
                        }
                    });
        }
    }

    private void runBlockHeaderCallbackTasks() {
        Queue<Runnable> tasks = getBlockHeaderCallbackTasks();
        setBlockHeaderCallbackTasks(new ConcurrentLinkedQueue<>());

        for (Runnable task : tasks) {
            threadPool.execute(task);
        }
    }

    private void onBlockHeader(long blockNumber, byte[] blockHeader) {
        if (blockHeaderCache.getBlockNumber() < blockNumber) {
            synchronized (this) {
                blockHeaderCache = new BlockHeaderCache(blockNumber, blockHeader);
                runBlockHeaderCallbackTasks();
            }
        }
    }

    private void clearTimeoutCallback() {
        synchronized (this) {
            runBlockHeaderCallbackTasks();
        }
    }

    public Queue<Runnable> getBlockHeaderCallbackTasks() {
        return blockHeaderCallbackTasks;
    }

    private void setBlockHeaderCallbackTasks(Queue<Runnable> blockHeaderCallbackTasks) {
        this.blockHeaderCallbackTasks = blockHeaderCallbackTasks;
    }

    public void setThreadPool(ThreadPoolTaskExecutor threadPool) {
        this.threadPool = threadPool;
    }

    public void setChain(Chain chain) {
        this.chain = chain;
    }
}
