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
    private String name;
    private Logger logger = LoggerFactory.getLogger(Chain.class);
    private Map<Peer, Connection> connections = new HashMap<Peer, Connection>();
    boolean hasLocalConnection = false;
    private Map<String, Resource> resources = new HashMap<String, Resource>();
    private Driver driver;
    private BlockHeaderStorage blockHeaderStorage;
    private Thread blockSyncThread;
    private AtomicBoolean running = new AtomicBoolean(false);
    private Random random = new SecureRandom();
    private BlockHeader localBlockHeader;

    public Chain(String name) {
        this.name = name;
    }

    public void start() {
        if (!running.get()) {
            running.set(true);

            blockSyncThread =
                    new Thread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    loadLocalBlockHeader();
                                    logger.trace("Block header sync thread started");
                                    while (running.get()) {
                                        try {
                                            Thread.sleep(1000);

                                            fetchBlockHeader();
                                        } catch (Exception e) {
                                            logger.error("Get block header error", e);
                                        }
                                    }
                                }
                            });

            blockSyncThread.start();
        }
    }

    public void stop() {
        if (running.get()) {
            running.set(false);
            try {
                logger.trace("Stoping block header sync thread...");
                blockSyncThread.interrupt();
                blockSyncThread.join();
                logger.trace("Block header sync thread stopped");
            } catch (InterruptedException e) {
                logger.error("Thread interrupt", e);
            }
        }
    }

    public void fetchBlockHeader() {
        Connection connection = chooseConnection();

        logger.trace("Fetch block header: {}", connection);
        if (connection != null) {
            long localBlockNumber = localBlockHeader.getNumber();
            String localBlockHash = localBlockHeader.getHash();
            long remoteBlockNumber = driver.getBlockNumber(connection);

            if (remoteBlockNumber > localBlockNumber) {
                for (long blockNumber = localBlockNumber + 1;
                        blockNumber <= remoteBlockNumber;
                        ++blockNumber) {
                    byte[] blockBytes = driver.getBlockHeader(blockNumber, connection);
                    if (blockBytes == null) {
                        logger.error(
                                "Could not get block header, please start the router which has chain({})",
                                name);
                        break;
                    }

                    BlockHeader blockHeader = driver.decodeBlockHeader(blockBytes);
                    if (localBlockNumber >= 0) {
                        if (blockHeader.getNumber() != localBlockHeader.getNumber() + 1
                                || !blockHeader.getPrevHash().equals(localBlockHeader.getHash())) {
                            logger.error(
                                    "Fetched block couldn't be the next block, localBlockNumber: {} localBlockHash: {} fetchedBlockNumber: {} fetchedBlockPrevHash: {}, please check the router which has chain({}) is the same chain?",
                                    localBlockNumber,
                                    localBlockHash,
                                    blockHeader.getNumber(),
                                    blockHeader.getPrevHash(),
                                    name);
                            break;
                        }
                    }

                    blockHeaderStorage.writeBlockHeader(blockNumber, blockBytes);
                    localBlockHeader = blockHeader; // Must update header after write in db

                    logger.debug("Commit blockHeader: {}", localBlockHeader.toString());
                }
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
        if (connections.isEmpty()) {
            logger.warn("Empty connections");
            return null;
        }

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

    private void loadLocalBlockHeader() {
        long localBlockNumber = blockHeaderStorage.readBlockNumber();
        if (localBlockNumber < 0) {
            BlockHeader beforeGenesisBlockHeader = new BlockHeader();
            beforeGenesisBlockHeader.setNumber(-1);
            beforeGenesisBlockHeader.setHash("");
            localBlockHeader = beforeGenesisBlockHeader;
        } else {
            byte[] blockHeaderBytes = blockHeaderStorage.readBlockHeader(localBlockNumber);
            localBlockHeader = driver.decodeBlockHeader(blockHeaderBytes);
        }
    }
}
