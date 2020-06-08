package com.webank.wecross.stubmanager;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.zone.Chain;
import io.netty.util.Timer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class MemoryBlockHeaderManager implements BlockHeaderManager {
    private Logger logger = LoggerFactory.getLogger(MemoryBlockHeaderManager.class);
    private ThreadPoolTaskExecutor threadPool;
    private Map<Long, List<GetBlockHeaderCallback>> getBlockHaderCallbacks =
            new HashMap<Long, List<GetBlockHeaderCallback>>();
    private LinkedList<BlockHeader> blockHeaderCache = new LinkedList<BlockHeader>();
    private Chain chain;
    private boolean running = false;
    private Timer timer;
    private long getBlockNumberDelay = 1000;
    private int maxCacheSize = 20;

    public void onGetBlockNumber(Exception e, long blockNumber) {
        long current = 0;
        if (!blockHeaderCache.isEmpty()) {
            current = blockHeaderCache.peekLast().getNumber();
        }

        if (current < blockNumber) {
            if (current == 0) {
                chain.getDriver()
                        .asyncGetBlockHeader(
                                blockNumber,
                                chain.chooseConnection(),
                                new Driver.GetBlockHeaderCallback() {
                                    @Override
                                    public void onResponse(Exception e, BlockHeader blockHeader) {
                                        onSyncBlockHeader(e, blockHeader, blockNumber);
                                    }
                                });
            } else {
                chain.getDriver()
                        .asyncGetBlockHeader(
                                current + 1,
                                chain.chooseConnection(),
                                new Driver.GetBlockHeaderCallback() {
                                    @Override
                                    public void onResponse(Exception e, BlockHeader blockHeader) {
                                        onSyncBlockHeader(e, blockHeader, blockNumber);
                                    }
                                });
            }
        } else {
            timer.newTimeout(
                    (timeout) -> {
                        chain.getDriver()
                                .asyncGetBlockNumber(
                                        chain.chooseConnection(),
                                        new Driver.GetBlockNumberCallback() {
                                            @Override
                                            public void onResponse(Exception e, long blockNumber) {
                                                onGetBlockNumber(e, blockNumber);
                                            }
                                        });
                    },
                    getBlockNumberDelay,
                    TimeUnit.MILLISECONDS);
        }
    }

    public void onSyncBlockHeader(Exception e, BlockHeader blockHeader, long target) {
        blockHeaderCache.add(blockHeader);
        List<GetBlockHeaderCallback> callbacks =
                getBlockHaderCallbacks.get(blockHeader.getNumber());
        if (callbacks != null) {
            for (GetBlockHeaderCallback callback : callbacks) {
                threadPool.execute(
                        new Runnable() {
                            @Override
                            public void run() {
                                callback.onResponse(null, blockHeader);
                            }
                        });
            }
        }
        getBlockHaderCallbacks.remove(blockHeader.getNumber());

        if (blockHeaderCache.size() > maxCacheSize) {
            blockHeaderCache.pop();
        }

        if (blockHeader.getNumber() < target) {
            chain.getDriver()
                    .asyncGetBlockHeader(
                            blockHeader.getNumber() + 1,
                            chain.chooseConnection(),
                            new Driver.GetBlockHeaderCallback() {
                                @Override
                                public void onResponse(Exception e, BlockHeader blockHeader) {
                                    onSyncBlockHeader(e, blockHeader, target);
                                }
                            });
        } else {
            chain.getDriver()
                    .asyncGetBlockNumber(
                            chain.chooseConnection(),
                            new Driver.GetBlockNumberCallback() {
                                @Override
                                public void onResponse(Exception e, long blockNumber) {
                                    onGetBlockNumber(e, blockNumber);
                                }
                            });
        }
    }

    @Override
    public void start() {
        if (!running) {
            logger.info("MemoryBlockHeaderManager started");

            running = true;

            chain.getDriver()
                    .asyncGetBlockNumber(
                            chain.chooseConnection(),
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
        if (running) {
            logger.info("MemoryBlockHeaderManager stopped");

            running = false;

            for (List<GetBlockHeaderCallback> callbacks : getBlockHaderCallbacks.values()) {
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

            blockHeaderCache.clear();
            getBlockHaderCallbacks.clear();
        }
    }

    @Override
    public void asyncGetBlockNumber(GetBlockNumberCallback callback) {
        threadPool.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        if (blockHeaderCache.isEmpty()) {
                            threadPool.execute(
                                    () -> {
                                        callback.onResponse(null, 0);
                                    });
                        } else {
                            threadPool.execute(
                                    () -> {
                                        callback.onResponse(
                                                null, blockHeaderCache.peekLast().getNumber());
                                    });
                        }
                    }
                });
    }

    @Override
    public void asyncGetBlockHeader(long blockNumber, GetBlockHeaderCallback callback) {
        if (blockHeaderCache.isEmpty() || blockNumber < blockHeaderCache.peekFirst().getNumber()) {
            chain.getDriver()
                    .asyncGetBlockHeader(
                            blockNumber,
                            chain.chooseConnection(),
                            new Driver.GetBlockHeaderCallback() {
                                @Override
                                public void onResponse(Exception e, BlockHeader blockHeader) {
                                    callback.onResponse(e, blockHeader);
                                }
                            });
        } else if (blockNumber > blockHeaderCache.peekLast().getNumber()) {
            if (!getBlockHaderCallbacks.containsKey(blockNumber)) {
                getBlockHaderCallbacks.put(blockNumber, new LinkedList<GetBlockHeaderCallback>());
            }

            getBlockHaderCallbacks.get(blockNumber).add(callback);
        } else {
            for (BlockHeader blockHeader : blockHeaderCache) {
                if (blockHeader.getNumber() == blockNumber) {
                    threadPool.execute(
                            new Runnable() {
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
