package com.webank.wecross.zone;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.peer.Peer;
import com.webank.wecross.remote.RemoteConnection;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.resource.ResourceBlockHeaderManager;
import com.webank.wecross.storage.BlockHeaderStorageFactory;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.StubManager;
import com.webank.wecross.utils.core.PathUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZoneManager {
    private Map<String, Zone> zones = new HashMap<>();
    private AtomicInteger seq = new AtomicInteger(1);
    private Logger logger = LoggerFactory.getLogger(ZoneManager.class);
    private P2PMessageEngine p2pEngine;
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private StubManager stubManager;
    BlockHeaderStorageFactory blockHeaderStorageFactory;

    public Resource getResource(Path path) {
        lock.readLock().lock();
        try {
            Zone zone = getZone(path);

            if (zone != null) {
                Chain stub = zone.getChain(path);

                if (stub != null) {
                    Resource resource = stub.getResources().get(path.getResource());

                    return resource;
                }
            }
            return null;
        } catch (Exception e) {
            logger.debug("Exception: " + e);
        } finally {
            lock.readLock().unlock();
        }
        return null;
    }

    public Zone getZone(Path path) {
        lock.readLock().lock();
        try {
            return getZone(path.getNetwork());
        } finally {
            lock.readLock().unlock();
        }
    }

    public Zone getZone(String name) {
        lock.readLock().lock();
        try {
            logger.trace("get zone: {}", name);
            Zone zone = zones.get(name);
            return zone;
        } finally {
            lock.readLock().unlock();
        }
    }

    public Map<String, Zone> getZones() {
        return zones;
    }

    public void setZones(Map<String, Zone> zones) {
        this.zones = zones;
    }

    public int getSeq() {
        return seq.intValue();
    }

    public void addRemoteResources(Peer peer, Map<String, ResourceInfo> resources)
            throws Exception {
        lock.writeLock().lock();
        try {
            for (Map.Entry<String, ResourceInfo> entry : resources.entrySet()) {
                Path path;
                ResourceInfo resourceInfo = entry.getValue();
                try {
                    path = Path.decode(entry.getKey());
                } catch (Exception e) {
                    logger.error("Parse path error: {} {}", entry.getKey(), e);
                    continue;
                }

                // Verify Checksum
                if (getResource(path) != null) {
                    String originChecksum = getResource(path).getResourceInfo().getChecksum();
                    String receiveChecksum = resourceInfo.getChecksum();
                    if (!originChecksum.equals(receiveChecksum)) {
                        logger.error(
                                "Receive resource with different checksum, ipath: {} peer: {} receiveChecksum: {} originChecksum: {}",
                                path.toString(),
                                peer.getNode().toString(),
                                receiveChecksum,
                                originChecksum);
                        continue;
                    }
                }

                Zone zone = zones.get(path.getNetwork());
                if (zone == null) {
                    zone = new Zone();
                    zones.put(path.getNetwork(), zone);
                }

                Driver driver = stubManager.getStubFactory(resourceInfo.getStubType()).newDriver();
                RemoteConnection remoteConnection = new RemoteConnection();
                remoteConnection.setP2pEngine(p2pEngine);
                remoteConnection.setPeer(peer);
                remoteConnection.setPath(path.toURI());

                Chain chain = zone.getChains().get(path.getChain());
                if (chain == null) {
                    chain = new Chain(path.getChain());
                    chain.setDriver(driver);

                    String blockPath = path.getNetwork() + "." + path.getChain();
                    chain.setBlockHeaderStorage(
                            blockHeaderStorageFactory.newBlockHeaderStorage(blockPath));
                    chain.addConnection(peer, remoteConnection);
                    chain.start();

                    logger.info("Start block header sync: {}", blockPath);

                    zone.getChains().put(path.getChain(), chain);
                } else {
                    chain.addConnection(peer, remoteConnection);
                }

                Resource resource = chain.getResources().get(path.getResource());
                if (resource == null) {
                    resource = new Resource();
                    resource.setDriver(driver);

                    ResourceBlockHeaderManager resourceBlockHeaderManager =
                            new ResourceBlockHeaderManager();
                    resourceBlockHeaderManager.setBlockHeaderStorage(chain.getBlockHeaderStorage());
                    resourceBlockHeaderManager.setChain(chain);

                    resource.setResourceBlockHeaderManager(resourceBlockHeaderManager);
                    resource.setResourceInfo(resourceInfo);

                    chain.getResources().put(path.getResource(), resource);
                }

                resource.addConnection(peer, remoteConnection);
                logger.info(
                        "Add remote resource({}) connection, peer: {}, resource: {}",
                        path.toString(),
                        peer.toString(),
                        resource.getResourceInfo());
            }
        } catch (WeCrossException e) {
            logger.error("Add remote resource error", e);
            throw e;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeRemoteResources(Peer peer, Map<String, ResourceInfo> resources) {
        lock.writeLock().lock();
        try {
            for (Map.Entry<String, ResourceInfo> entry : resources.entrySet()) {
                Path path;
                try {
                    path = Path.decode(entry.getKey());
                } catch (Exception e) {
                    logger.error("Parse path error: {} {}", entry.getKey(), e);
                    continue;
                }

                Zone zone = zones.get(path.getNetwork());
                if (zone == null) {
                    // zone not exists, bug?
                    logger.error("Zone not exists! Peer: {} Path: {}", peer, path);
                    continue;
                }

                Chain chain = zone.getChain(path.getChain());
                if (chain == null) {
                    // stub not exists, bug?
                    logger.error("Stub not exists! Peer: {} Path: {}", peer, path);
                    continue;
                }

                Resource resource = chain.getResources().get(path.getResource());

                if (resource == null) {
                    // resource not exists, bug?
                    logger.error("Resource not exists! Peer: {} Path: {}", peer, path);
                    continue;
                }

                resource.removeConnection(peer);

                if (resource.isConnectionEmpty()) {
                    chain.getResources().remove(path.getResource());
                }

                if (chain.getResources().isEmpty()) {
                    chain.stop();
                    if (chain.getBlockHeaderStorage() != null) {
                        chain.getBlockHeaderStorage().close();
                    }
                    logger.info(
                            "Stop block header sync: {}",
                            path.getNetwork() + "." + path.getChain());

                    zone.getChains().remove(path.getChain());
                }

                if (zone.getChains().isEmpty()) {
                    zones.remove(path.getNetwork());
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Map<String, Resource> getAllResources(boolean ignoreRemote) {
        Map<String, Resource> resources = new HashMap<String, Resource>();

        lock.readLock().lock();
        try {
            for (Map.Entry<String, Zone> zoneEntry : zones.entrySet()) {
                String zoneName = PathUtils.toPureName(zoneEntry.getKey());

                for (Map.Entry<String, Chain> stubEntry :
                        zoneEntry.getValue().getChains().entrySet()) {
                    String stubName = PathUtils.toPureName(stubEntry.getKey());

                    for (Map.Entry<String, Resource> resourceEntry :
                            stubEntry.getValue().getResources().entrySet()) {
                        if (resourceEntry.getValue().isHasLocalConnection() || !ignoreRemote) {
                            String resourceName = PathUtils.toPureName(resourceEntry.getKey());
                            resources.put(
                                    zoneName + "." + stubName + "." + resourceName,
                                    resourceEntry.getValue());
                        }
                    }
                }
            }
        } finally {
            lock.readLock().unlock();
        }

        return resources;
    }

    public Map<String, ResourceInfo> getAllResourcesInfo(boolean ignoreRemote) {
        Map<String, ResourceInfo> resources = new HashMap<String, ResourceInfo>();

        lock.readLock().lock();
        try {
            for (Map.Entry<String, Zone> zoneEntry : zones.entrySet()) {
                String zoneName = PathUtils.toPureName(zoneEntry.getKey());

                for (Map.Entry<String, Chain> stubEntry :
                        zoneEntry.getValue().getChains().entrySet()) {
                    String stubName = PathUtils.toPureName(stubEntry.getKey());

                    for (Map.Entry<String, Resource> resourceEntry :
                            stubEntry.getValue().getResources().entrySet()) {
                        if (ignoreRemote && !resourceEntry.getValue().isHasLocalConnection()) {
                        } else {
                            String resourceName = PathUtils.toPureName(resourceEntry.getKey());
                            resources.put(
                                    zoneName + "." + stubName + "." + resourceName,
                                    resourceEntry.getValue().getResourceInfo());
                        }
                    }
                }
            }
        } finally {
            lock.readLock().unlock();
        }

        return resources;
    }

    public P2PMessageEngine getP2PEngine() {
        return p2pEngine;
    }

    public void setP2PEngine(P2PMessageEngine p2pEngine) {
        this.p2pEngine = p2pEngine;
    }

    public StubManager getStubManager() {
        return stubManager;
    }

    public void setStubManager(StubManager stubManager) {
        this.stubManager = stubManager;
    }

    public BlockHeaderStorageFactory getBlockHeaderStorageFactory() {
        return blockHeaderStorageFactory;
    }

    public void setBlockHeaderStorageFactory(BlockHeaderStorageFactory blockHeaderStorageFactory) {
        this.blockHeaderStorageFactory = blockHeaderStorageFactory;
    }
}
