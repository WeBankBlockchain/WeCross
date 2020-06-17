package com.webank.wecross.zone;

import com.webank.wecross.peer.Peer;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.BlockHeaderManager;
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
    private ChainInfo chainInfo;
    private Map<Peer, Connection> connections = new HashMap<Peer, Connection>();
    boolean hasLocalConnection = false;
    private Map<String, Resource> resources = new HashMap<String, Resource>();
    private Driver driver;
    private BlockHeaderManager blockHeaderManager;
    private AtomicBoolean running = new AtomicBoolean(false);
    private Random random = new SecureRandom();

    public Chain(ChainInfo chainInfo) {
        this.chainInfo = chainInfo;
    }

    public void start() {
        if (!running.get()) {
            running.set(true);
        }
    }

    public void stop() {
        if (running.get()) {
            running.set(false);
            try {
            } catch (Exception e) {
                logger.error("Thread interrupt", e);
            }
        }
    }

    public ChainInfo getChainInfo() {
        return chainInfo;
    }

    public void setChainInfo(ChainInfo chainInfo) {
        this.chainInfo = chainInfo;
    }

    public Map<Peer, Connection> getConnection() {
        return connections;
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

    public BlockHeaderManager getBlockHeaderManager() {
        return blockHeaderManager;
    }

    public void setBlockHeaderManager(BlockHeaderManager blockHeaderManager) {
        this.blockHeaderManager = blockHeaderManager;
    }
}
