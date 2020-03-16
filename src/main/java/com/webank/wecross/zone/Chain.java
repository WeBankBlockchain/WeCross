package com.webank.wecross.zone;

import com.webank.wecross.peer.Peer;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.storage.BlockHeaderStorage;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Chain {
    private Logger logger = LoggerFactory.getLogger(Chain.class);
    private Map<Peer, Connection> connections = new HashMap<Peer, Connection>();
    boolean hasLocalConnection = false;
    private Map<String, Resource> resources = new HashMap<String, Resource>();
    private Driver driver;
    private BlockHeaderStorage blockHeaderStorage;
    private Thread blockSyncThread;
    private AtomicBoolean running = new AtomicBoolean(false);
    private Random random = new SecureRandom();

    public void start() {
        if (!running.get()) {
            running.set(true);

            blockSyncThread =
                    new Thread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    while (running.get()) {
                                        try {
                                            Thread.sleep(1000);

                                            Connection connection = chooseConnection();

                                            long localBlockNumber =
                                                    blockHeaderStorage.readBlockNumber();
                                            long remoteBlockNumber =
                                                    driver.getBlockNumber(connection);

                                            if (remoteBlockNumber > localBlockNumber) {
                                                for (long blockNumber = localBlockNumber + 1;
                                                        blockNumber < remoteBlockNumber;
                                                        ++blockNumber) {
                                                    byte[] blockBytes =
                                                            driver.getBlockHeader(
                                                                    blockNumber, connection);
                                                    blockHeaderStorage.writeBlockHeader(
                                                            blockNumber, blockBytes);
                                                }
                                            }
                                        } catch (InterruptedException e) {
                                            logger.info("Block sync thread interrupt");
                                            break;
                                        } catch (Exception e) {
                                            logger.error("Get block header error", e);
                                        }
                                    }
                                }
                            });
        }
    }

    public void stop() {
        if (running.get()) {
            running.set(false);
            try {
                blockSyncThread.join();
            } catch (InterruptedException e) {
                logger.error("Thread interrupt", e);
            }
        }
    }

    public long getBlockNumber() {
        return blockHeaderStorage.readBlockNumber();
    }

    public BlockHeader getBlockHeader(int blockNumber) {
        return driver.decodeBlockHeader(blockHeaderStorage.readBlockHeader(blockNumber));
    }

    public void putBlockHeader(int blockNumber, byte[] blockHeader) {
        blockHeaderStorage.writeBlockHeader(blockNumber, blockHeader);
    }

    public void addConnection(Peer peer, Connection connection) {
        if (!hasLocalConnection) {
            if (peer == null) {
                connections.clear();
                hasLocalConnection = true;
            }
            connections.put(peer, connection);
        }
    }

    public void removeConnection(Peer peer) {
        if (!hasLocalConnection) {
            connections.remove(peer);
        }
    }

    public Connection chooseConnection() {
        if (connections.size() == 1) {
            return (Connection) connections.values().toArray()[0];
        } else {
            int index = random.nextInt(connections.size());
            return (Connection) connections.values().toArray()[index];
        }
    }

    public Map<String, Resource> getResources() {
        return resources;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setResources(Map<String, Resource> resources) {
        this.resources = resources;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public BlockHeaderStorage getBlockHeaderStorage() {
        return blockHeaderStorage;
    }

    public void setBlockHeaderStorage(BlockHeaderStorage blockHeaderStorage) {
        this.blockHeaderStorage = blockHeaderStorage;
    }
}
