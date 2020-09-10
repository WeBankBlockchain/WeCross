package com.webank.wecross.zone;

import com.webank.wecross.peer.Peer;
import com.webank.wecross.remote.RemoteConnection;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.BlockManager;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.ResourceInfo;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Chain {
    private Logger logger = LoggerFactory.getLogger(Chain.class);

    // chain Info
    private String zoneName;
    private String name;
    private String stubType;
    private Map<String, String> properties;
    private String checksum;

    private Connection localConnection;
    private Set<Peer> peers = new HashSet<>();
    private Map<String, Resource> resources = new HashMap<String, Resource>();
    private Driver driver;
    private BlockManager blockManager;
    private Random random = new SecureRandom();
    private ReadWriteLock lock = new ReentrantReadWriteLock();

    public Chain(String zoneName, ChainInfo chainInfo, Driver driver, Connection localConnection) {

        this.zoneName = zoneName;
        this.name = chainInfo.getName();
        this.stubType = chainInfo.getStubType();
        this.checksum = chainInfo.getChecksum();
        this.driver = driver;
        this.localConnection = localConnection;

        if (localConnection != null) {
            this.properties = localConnection.getProperties();
        } else {
            this.properties = chainInfo.getProperties();
        }
    }

    public void start() {
        blockManager.start();
    }

    public void stop() {
        blockManager.stop();
    }

    public ChainInfo getChainInfo() {
        ChainInfo chainInfo = new ChainInfo();
        chainInfo.setName(name);
        chainInfo.setStubType(stubType);
        chainInfo.setProperties(properties);
        chainInfo.setChecksum(checksum);

        List<ResourceInfo> resourceInfos = getAllResourcesInfo(true);
        chainInfo.setResources(resourceInfos);
        return chainInfo;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setResources(Map<String, Resource> resources) {
        this.resources = resources;
    }

    public String getStubType() {
        return stubType;
    }

    public void setStubType(String stubType) {
        this.stubType = stubType;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public BlockManager getBlockManager() {
        return blockManager;
    }

    public void setBlockManager(BlockManager blockManager) {
        this.blockManager = blockManager;
    }

    public long getBlockNumber() {
        long blockNumber = 0;
        try {
            CompletableFuture<Long> future = new CompletableFuture<>();
            this.blockManager.asyncGetBlockNumber(
                    new BlockManager.GetBlockNumberCallback() {
                        @Override
                        public void onResponse(Exception e, long blockNumber) {
                            if (e != null) {
                                logger.warn("getBlockNumber exception: " + e);
                                future.complete(Long.valueOf(0));
                            } else {
                                future.complete(blockNumber);
                            }
                        }
                    });
            blockNumber = future.get(10, TimeUnit.SECONDS).longValue();
        } catch (Exception e) {
            logger.warn("getBlockNumber exception: " + e);
            blockNumber = 0;
        }
        return blockNumber;
    }

    public Map<Peer, Connection> getConnections() {
        lock.readLock().lock();
        try {
            Map<Peer, Connection> connections = new HashMap<>();
            if (localConnection != null) {
                connections.put(null, localConnection); // null means local connection in Resource
            }

            for (Resource resource : resources.values()) {
                for (Map.Entry<Peer, Connection> entry : resource.getConnections().entrySet())
                    if (!connections.containsKey(entry.getKey())) {
                        connections.put(entry.getKey(), entry.getValue());
                    }
            }

            if (connections.size() == 0) {
                logger.warn("getConnections: Chain {} has no connection", name);
            }

            return connections;
        } catch (Exception e) {
            logger.debug("Exception: " + e);
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public Set<Peer> getPeers() {
        return peers;
    }

    public Connection chooseConnection() {
        Map<Peer, Connection> connections = getConnections();
        if (connections == null) {
            logger.warn("Chain {} connection is null", name);
            return null;
        }

        Connection connection = connections.get(null);
        if (connection != null) {
            return connection;
        }

        if (connections.size() == 0) {
            return null;
        } else {
            int index = random.nextInt(connections.size());
            return (Connection) connections.values().toArray()[index];
        }
    }

    public void addResource(Path path, Resource resource, boolean replaceIfExist) {
        addResource(path.getResource(), resource, replaceIfExist);
    }

    public void addResource(String name, Resource resource, boolean replaceIfExist) {
        Resource oldResource = getResource(name);
        lock.writeLock().lock();
        try {

            if (oldResource != null && replaceIfExist || oldResource == null) {
                resources.put(name, resource);
            }
        } catch (Exception e) {
            logger.debug("Exception: " + e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeResource(Path path, boolean ignoreRemote) {
        removeResource(path.getResource(), ignoreRemote);
    }

    public void removeResource(String name, boolean ignoreRemote) {
        Resource resource = getResource(name);
        if (resource != null && !resource.hasLocalConnection() && ignoreRemote) {
            return; // ignore remote resource
        }

        lock.writeLock().lock();
        try {
            if (resources.containsKey(name)) {

                resources.remove(name);
            }
        } catch (Exception e) {
            logger.debug("Exception: " + e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Resource getResource(String name) {
        lock.readLock().lock();
        try {
            return resources.get(name);
        } catch (Exception e) {
            logger.debug("Exception: " + e);
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public Resource getResource(Path path) {
        return getResource(path.getResource());
    }

    public Map<String, Resource> getResources() {
        lock.readLock().lock();
        try {
            return resources;
        } catch (Exception e) {
            logger.debug("Exception: " + e);
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<ResourceInfo> getAllResourcesInfo(boolean ignoreRemote) {
        List<ResourceInfo> resourceInfos = new LinkedList<>();
        for (Resource resource : resources.values()) {
            if (ignoreRemote && !resource.hasLocalConnection()) {
                continue;
            }
            resourceInfos.add(resource.getResourceInfo());
        }
        return resourceInfos;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public void updateLocalResources(List<ResourceInfo> resourceInfos) {
        lock.writeLock().lock();
        try {
            // Remove old local resources
            List<String> oldResourceNames = new LinkedList<>();
            for (Resource resource : resources.values()) {
                if (resource.isOnlyLocal()) {
                    oldResourceNames.add(resource.getResourceInfo().getName());
                }
            }
            for (String oldResource : oldResourceNames) {
                resources.remove(oldResource);
            }

            // Add new local resources
            for (ResourceInfo newResourceInfo : resourceInfos) {
                String newResourceName = newResourceInfo.getName();
                Resource resource = resources.get(newResourceName);

                if (resource == null) {
                    resource = new Resource();
                    resources.put(newResourceName, resource);
                }

                Path path = new Path();
                path.setZone(zoneName);
                path.setChain(name);
                path.setResource(newResourceName);
                resource.setPath(path);
                resource.setStubType(stubType);
                resource.setTemporary(false);
                resource.setResourceInfo(newResourceInfo);
                resource.setBlockManager(blockManager);
                resource.setDriver(driver);
                resource.addConnection(null, localConnection);

                logger.info(
                        "Chain {} add/update local resource {}", name, resource.getResourceInfo());
            }

        } catch (Exception e) {
            logger.debug("Exception: " + e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void addRemoteResource(
            Peer peer, ResourceInfo resourceInfo, RemoteConnection remoteConnection) {
        lock.writeLock().lock();
        try {

            if (!peers.contains(peer)) {
                peers.add(peer);
            }

            // Add or append resource's remote connection
            String newName = resourceInfo.getName();
            Resource resource = resources.get(newName);

            if (resource == null) {
                resource = new Resource();
                resources.put(newName, resource);
            }

            Path path = new Path();
            path.setZone(zoneName);
            path.setChain(name);
            path.setResource(newName);
            resource.setPath(path);
            resource.setStubType(stubType);
            resource.setTemporary(false);
            resource.setResourceInfo(resourceInfo);
            resource.setBlockManager(blockManager);
            resource.setDriver(driver);
            resource.addConnection(peer, remoteConnection);

            logger.info(
                    "Chain {} add/update remote resource {}, peer: {}",
                    name,
                    resource.getResourceInfo(),
                    peer.toString());

        } catch (Exception e) {
            logger.debug("Exception: " + e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removePeers(Peer peer) {
        lock.writeLock().lock();
        try {
            if (peers.contains(peer)) {
                peers.remove(peer);
            }
        } catch (Exception e) {
            logger.debug("Exception: " + e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean hasLocalConnection() {
        return localConnection != null;
    }

    public Connection getLocalConnection() {
        return localConnection;
    }
}
