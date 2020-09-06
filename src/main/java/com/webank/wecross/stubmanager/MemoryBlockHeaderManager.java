package com.webank.wecross.stubmanager;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.BlockHeaderData;
import com.webank.wecross.stub.BlockHeaderManager;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.zone.Chain;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class MemoryBlockHeaderManager implements BlockHeaderManager {
    private Logger logger = LoggerFactory.getLogger(MemoryBlockHeaderManager.class);
    private ThreadPoolTaskExecutor threadPool;
    private Map<Long, List<GetBlockHeaderCallback>> getBlockHeaderCallbacks =
            new HashMap<Long, List<GetBlockHeaderCallback>>();
    private LinkedList<BlockHeaderData> blockHeaderDataCache = new LinkedList<BlockHeaderData>();
    private Chain chain;
    private AtomicBoolean running = new AtomicBoolean(false);
    private Timer timer;
    private Timeout timeout;
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private long getBlockNumberDelay = 1000;
    private int maxCacheSize = 20;

    public void onGetBlockNumber(Exception e, long blockNumber) {
        lock.readLock().lock();
        long current = 0;
        try {
            if (!blockHeaderDataCache.isEmpty()) {
                current = blockHeaderDataCache.peekLast().getBlockHeader().getNumber();
            }
        } finally {
            lock.readLock().unlock();
        }

        if (current < blockNumber) {
            long blockNumberToGet = 0;
            if (current == 0) {
                blockNumberToGet = blockNumber;
            } else {
                blockNumberToGet = current + 1;
            }

            chain.getDriver()
                    .asyncGetBlock(
                            blockNumberToGet,
                            true,
                            chain.chooseConnection(),
                            (error, data) -> {
                                onSyncBlockHeader(
                                        error,
                                        new BlockHeaderData(
                                                data.getBlockHeader(), data.getRawBytes()),
                                        blockNumber);
                            });

        } else {
            waitAndSyncBlock(getGetBlockNumberDelay());
        }
    }

    public void onSyncBlockHeader(Exception e, BlockHeaderData blockHeaderData, long target) {
        if (Objects.nonNull(e)) {
            logger.warn("On block header exception: ", e);
            waitAndSyncBlock(getGetBlockNumberDelay());
            return;
        }

        BlockHeader blockHeader = blockHeaderData.getBlockHeader();
        byte[] data = blockHeaderData.getData();
        lock.writeLock().lock();

        try {

            blockHeaderDataCache.add(blockHeaderData);
            List<GetBlockHeaderCallback> callbacks =
                    getBlockHeaderCallbacks.get(blockHeader.getNumber());
            if (callbacks != null) {
                for (GetBlockHeaderCallback callback : callbacks) {
                    threadPool.execute(
                            new Runnable() {
                                @Override
                                public void run() {
                                    callback.onResponse(null, blockHeaderData);
                                }
                            });
                }
            }
            getBlockHeaderCallbacks.remove(blockHeader.getNumber());

            if (blockHeaderDataCache.size() > maxCacheSize) {
                blockHeaderDataCache.pop();
            }

        } finally {
            lock.writeLock().unlock();
        }

        if (blockHeader.getNumber() < target) {
            chain.getDriver()
                    .asyncGetBlock(
                            blockHeader.getNumber() + 1,
                            true,
                            chain.chooseConnection(),
                            (error, blockData) -> {
                                onSyncBlockHeader(
                                        error,
                                        new BlockHeaderData(
                                                blockData.getBlockHeader(),
                                                blockData.getRawBytes()),
                                        target);
                            });
        } else {
            waitAndSyncBlock(0);
        }
    }

    private void waitAndSyncBlock(long delay) {
        timeout =
                timer.newTimeout(
                        (timeout) -> {
                            chain.getDriver()
                                    .asyncGetBlockNumber(
                                            chain.chooseConnection(),
                                            new Driver.GetBlockNumberCallback() {
                                                @Override
                                                public void onResponse(
                                                        Exception e, long blockNumber) {
                                                    onGetBlockNumber(e, blockNumber);
                                                }
                                            });
                        },
                        delay,
                        TimeUnit.MILLISECONDS);
    }

    @Override
    public void start() {
        Connection connection = chain.chooseConnection();
        Driver driver = chain.getDriver();
        if (connection != null && driver != null && running.compareAndSet(false, true)) {
            logger.info("MemoryBlockHeaderManager started");

            chain.getDriver()
                    .asyncGetBlockNumber(
                            connection,
                            new Driver.GetBlockNumberCallback() {
                                @Override
                                public void onResponse(Exception e, long blockNumber) {
                                    onGetBlockNumber(e, blockNumber);
                                }
                            });
        }
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            logger.info("MemoryBlockHeaderManager stopped");

            for (List<GetBlockHeaderCallback> callbacks : getBlockHeaderCallbacks.values()) {
                for (GetBlockHeaderCallback callback : callbacks) {
                    threadPool.execute(
                            new Runnable() {
                                @Override
                                public void run() {
                                    callback.onResponse(
                                            new WeCrossException(-1, "Operation canceled"), null);
                                }
                            });
                }
            }

            blockHeaderDataCache.clear();
            getBlockHeaderCallbacks.clear();
            if (timeout != null) {
                timeout.cancel();
            }
        }
    }

    @Override
    public void asyncGetBlockNumber(GetBlockNumberCallback callback) {
        lock.readLock().lock();
        try {
            if (blockHeaderDataCache.isEmpty()) {
                threadPool.execute(
                        () -> {
                            callback.onResponse(null, 0);
                        });
            } else {
                threadPool.execute(
                        () -> {
                            callback.onResponse(
                                    null,
                                    blockHeaderDataCache.peekLast().getBlockHeader().getNumber());
                        });
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void asyncGetBlockHeader(long blockNumber, GetBlockHeaderCallback callback) {
        lock.writeLock().lock();

        try {
            if (blockHeaderDataCache.isEmpty()
                    || blockNumber
                            < blockHeaderDataCache.peekFirst().getBlockHeader().getNumber()) {
                chain.getDriver()
                        .asyncGetBlock(
                                blockNumber,
                                true,
                                chain.chooseConnection(),
                                (error, data) -> {
                                    callback.onResponse(
                                            error,
                                            new BlockHeaderData(
                                                    data.getBlockHeader(), data.getRawBytes()));
                                });
            } else if (blockNumber > blockHeaderDataCache.peekLast().getBlockHeader().getNumber()) {
                if (!getBlockHeaderCallbacks.containsKey(blockNumber)) {
                    getBlockHeaderCallbacks.put(
                            blockNumber, new LinkedList<GetBlockHeaderCallback>());
                }

                getBlockHeaderCallbacks.get(blockNumber).add(callback);

                if (timeout != null) {
                    if (timeout.cancel()) {
                        threadPool.execute(
                                () -> {
                                    try {
                                        timeout.task().run(timeout);
                                    } catch (Exception e) {
                                        logger.error("Unexcept exception", e);
                                    }
                                });
                    }
                }
            } else {
                for (BlockHeaderData blockHeaderData : blockHeaderDataCache) {
                    if (blockHeaderData.getBlockHeader().getNumber() == blockNumber) {
                        threadPool.execute(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.onResponse(null, blockHeaderData);
                                    }
                                });

                        break;
                    }
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public ThreadPoolTaskExecutor getThreadPool() {
        return threadPool;
    }

    public void setThreadPool(ThreadPoolTaskExecutor threadPool) {
        this.threadPool = threadPool;
    }

    public Chain getChain() {
        return chain;
    }

    public void setChain(Chain chain) {
        this.chain = chain;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public long getGetBlockNumberDelay() {
        return getBlockNumberDelay;
    }

    public void setGetBlockNumberDelay(long getBlockNumberDelay) {
        this.getBlockNumberDelay = getBlockNumberDelay;
    }

    public int getMaxCacheSize() {
        return maxCacheSize;
    }

    public void setMaxCacheSize(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }
}
