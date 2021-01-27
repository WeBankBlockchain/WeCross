package com.webank.wecross.stubmanager;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.stub.Block;
import com.webank.wecross.stub.BlockManager;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class MemoryBlockManager implements BlockManager {
    private static final Logger logger = LoggerFactory.getLogger(MemoryBlockManager.class);

    private ThreadPoolTaskExecutor threadPool;
    private Map<Long, List<GetBlockCallback>> getBlockCallbacks =
            new HashMap<Long, List<GetBlockCallback>>();
    private LinkedList<Block> blockDataCache = new LinkedList<>();
    private Chain chain;
    private AtomicBoolean running = new AtomicBoolean(false);
    private Timer timer;
    private Timeout timeout;
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private long getBlockNumberDelay = 1000;
    private int maxCacheSize = 20;
    private AtomicLong latestBlockNumber = new AtomicLong(-1L);

    private static class Status {
        public static final int Starting = 0;
        public static final int OK = 1;
        public static final int Failure = 2;
    }

    private AtomicInteger fetchBlockNumberStatus = new AtomicInteger(Status.Starting);

    public void onGetBlockNumber(Exception e, long blockNumber) {

        if (Objects.nonNull(e)) {
            logger.warn("onGetBlockNumber failed, e: ", e);
            fetchBlockNumberStatus.set(Status.Failure);
            waitAndSyncBlock(getGetBlockNumberDelay());
            return;
        }

        // reset latestBlockNumber field
        latestBlockNumber.set(blockNumber);
        fetchBlockNumberStatus.set(Status.OK);

        if (logger.isTraceEnabled()) {
            logger.trace("onGetBlockNumber, blockNumber: {}", blockNumber);
        }

        lock.readLock().lock();
        long current = 0;
        try {
            if (!blockDataCache.isEmpty()) {
                current = blockDataCache.peekLast().getBlockHeader().getNumber();
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
                                onSyncBlock(error, data, blockNumber);
                            });

        } else {
            waitAndSyncBlock(getGetBlockNumberDelay());
        }
    }

    public void onSyncBlock(Exception e, Block block, long target) {
        if (Objects.nonNull(e)) {
            logger.warn("onSyncBlock failed, e: ", e);
            waitAndSyncBlock(getGetBlockNumberDelay());
            return;
        }

        if (logger.isTraceEnabled()) {
            logger.trace(
                    "On sync block, blockNumber: {}, blockHash: {}",
                    block.getBlockHeader().getNumber(),
                    block.getBlockHeader().getHash());
        }

        lock.writeLock().lock();

        try {

            blockDataCache.add(block);
            List<GetBlockCallback> callbacks =
                    getBlockCallbacks.get(block.getBlockHeader().getNumber());
            if (callbacks != null) {
                for (GetBlockCallback callback : callbacks) {
                    threadPool.execute(
                            new Runnable() {
                                @Override
                                public void run() {
                                    callback.onResponse(null, block);
                                }
                            });
                }
            }
            getBlockCallbacks.remove(block.getBlockHeader().getNumber());

            if (blockDataCache.size() > maxCacheSize) {
                blockDataCache.pop();
            }

        } finally {
            lock.writeLock().unlock();
        }

        if (block.getBlockHeader().getNumber() < target) {
            chain.getDriver()
                    .asyncGetBlock(
                            block.getBlockHeader().getNumber() + 1,
                            true,
                            chain.chooseConnection(),
                            (error, blockData) -> {
                                onSyncBlock(error, blockData, target);
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
                                    if (Objects.isNull(e)) {
                                        logger.info(
                                                "MemoryBlockManager initialize successfully, blockNumber: {}",
                                                blockNumber);
                                    } else {
                                        logger.error(
                                                "MemoryBlockManager initialize failed, e: ", e);
                                    }
                                }
                            });
        }
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            logger.info("MemoryBlockHeaderManager stopped");

            for (List<GetBlockCallback> callbacks : getBlockCallbacks.values()) {
                for (GetBlockCallback callback : callbacks) {
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

            blockDataCache.clear();
            getBlockCallbacks.clear();
            if (timeout != null) {
                timeout.cancel();
            }
        }
    }

    @Override
    public void asyncGetBlockNumber(GetBlockNumberCallback callback) {

        long blockNumber = 0;
        WeCrossException e = null;
        if (fetchBlockNumberStatus.get() == Status.OK) {
            blockNumber = latestBlockNumber.get();
        } else {
            e =
                    new WeCrossException(
                            WeCrossException.ErrorCode.GET_BLOCK_NUMBER_ERROR,
                            "get blockNumber failed");
        }

        long finalBlockNumber = blockNumber;
        Exception finalE = e;
        threadPool.execute(
                () -> {
                    callback.onResponse(finalE, finalBlockNumber);
                });
    }

    @Override
    public void asyncGetBlock(long blockNumber, GetBlockCallback callback) {
        lock.writeLock().lock();

        try {
            if (blockDataCache.isEmpty()
                    || blockNumber < blockDataCache.peekFirst().getBlockHeader().getNumber()) {
                chain.getDriver()
                        .asyncGetBlock(
                                blockNumber,
                                true,
                                chain.chooseConnection(),
                                (error, data) -> {
                                    callback.onResponse(error, data);
                                });
            } else if (blockNumber > blockDataCache.peekLast().getBlockHeader().getNumber()) {
                if (!getBlockCallbacks.containsKey(blockNumber)) {
                    getBlockCallbacks.put(blockNumber, new LinkedList<GetBlockCallback>());
                }

                getBlockCallbacks.get(blockNumber).add(callback);

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
                for (Block block : blockDataCache) {
                    if (block.getBlockHeader().getNumber() == blockNumber) {
                        threadPool.execute(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.onResponse(null, block);
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
