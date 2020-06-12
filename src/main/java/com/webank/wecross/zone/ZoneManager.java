package com.webank.wecross.zone;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.network.p2p.P2PService;
import com.webank.wecross.peer.Peer;
import com.webank.wecross.remote.RemoteConnection;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stubmanager.MemoryBlockHeaderManager;
import com.webank.wecross.stubmanager.MemoryBlockHeaderManagerFactory;
import com.webank.wecross.stubmanager.StubManager;
import com.webank.wecross.utils.core.PathUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZoneManager {
    private Logger logger = LoggerFactory.getLogger(ZoneManager.class);
    private Map<String, Zone> zones = new HashMap<>();
    private AtomicInteger seq = new AtomicInteger(1);
    private P2PService p2PService;
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private StubManager stubManager;
    private MemoryBlockHeaderManagerFactory memoryBlockHeaderManagerFactory;

    public Chain getChain(Path path) {
        lock.readLock().lock();
        try {
            Zone zone = getZone(path);

            if (zone != null) {
                Chain chain = zone.getChain(path);

                return chain;
            }
        } catch (Exception e) {
            logger.debug("Exception: " + e);
        } finally {
            lock.readLock().unlock();
        }

        return null;
    }

    public Resource fetchResource(Path path) {
        return getResource(path, true);
    }

    public Resource getResource(Path path) {
        return getResource(path, false);
    }

    public Resource getResource(Path path, boolean create) {
        lock.readLock().lock();
        try {
            Zone zone = getZone(path);

            if (zone != null) {
                Chain chain = zone.getChain(path);

                if (chain != null) {
                    Resource resource = chain.getResources().get(path.getResource());

                    if (resource != null) {
                        return resource;
                    } else {
                        if (create) {
                            ResourceInfo resourceInfo = new ResourceInfo();
                            resourceInfo.setName(path.getResource());

                            // not found, build default resource
                            resource = new Resource();
                            resource.setBlockHeaderManager(chain.getBlockHeaderManager());
                            resource.setDriver(chain.getDriver());
                            resource.setType("TemporaryResource");
                            resource.setResourceInfo(resourceInfo);

                            resource.setConnection(chain.getConnection());
                            resource.setTemporary(true);

                            // TODO: comment?
                            // chain.getResources().put(path.getResource(), resource);
                            return resource;
                        } else {
                            return null;
                        }
                    }
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
            return getZone(path.getZone());
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

    public void addRemoteChains(Peer peer, Map<String, ChainInfo> chainInfos) throws Exception {
        lock.writeLock().lock();
        try {
            for (Map.Entry<String, ChainInfo> entry : chainInfos.entrySet()) {
                Path chainPath;
                ChainInfo chainInfo = entry.getValue();
                try {
                    chainPath = Path.decode(entry.getKey());
                } catch (Exception e) {
                    logger.error("Parse path error: {} {}", entry.getKey(), e);
                    continue;
                }

                /*
                // Verify Checksum
                if (getChain(path) != null) {
                    String originChecksum = getChain(path).getChainInfo().
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
                */

                Zone zone = zones.get(chainPath.getZone());
                if (zone == null) {
                    zone = new Zone();
                    zones.put(chainPath.getZone(), zone);
                }

                Driver driver = stubManager.getStubFactory(chainInfo.getStubType()).newDriver();
                RemoteConnection remoteConnection = new RemoteConnection();
                remoteConnection.setP2PService(p2PService);
                remoteConnection.setPeer(peer);
                remoteConnection.setPath(chainPath.toURI());
                remoteConnection.setProperties(chainInfo.getProperties());

                Chain chain = zone.getChains().get(chainPath.getChain());
                if (chain == null) {
                    chain = new Chain(entry.getValue());
                    chain.setDriver(driver);

                    MemoryBlockHeaderManager resourceBlockHeaderManager =
                            memoryBlockHeaderManagerFactory.build(chain);

                    chain.setBlockHeaderManager(resourceBlockHeaderManager);

                    chain.addConnection(peer, remoteConnection);
                    chain.start();

                    logger.info(
                            "Start block header sync: {}",
                            chainPath.getZone() + "." + chainPath.getChain());

                    zone.getChains().put(chainPath.getChain(), chain);
                } else {
                    chain.addConnection(peer, remoteConnection);
                }

                if (entry.getValue().getResources() != null) {
                    for (ResourceInfo resourceInfo : entry.getValue().getResources()) {
                        Resource resource = chain.getResources().get(resourceInfo.getName());
                        if (resource == null) {
                            resource = new Resource();
                            resource.setDriver(driver);

                            resource.setBlockHeaderManager(chain.getBlockHeaderManager());
                            resource.setResourceInfo(resourceInfo);

                            chain.getResources().put(resourceInfo.getName(), resource);
                        }

                        resource.setTemporary(false);
                        resource.addConnection(peer, remoteConnection);
                        logger.info(
                                "Add remote resource({}) connection, peer: {}, resource: {}",
                                chainPath.toString(),
                                peer.toString(),
                                resource.getResourceInfo());
                    }
                }
            }
        } catch (WeCrossException e) {
            logger.error("Add remote resource error", e);
            throw e;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeRemoteChains(
            Peer peer, Map<String, ChainInfo> chains, boolean removeChainConnection) {
        lock.writeLock().lock();
        try {
            for (Map.Entry<String, ChainInfo> entry : chains.entrySet()) {
                Path chainPath;
                try {
                    chainPath = Path.decode(entry.getKey());
                } catch (Exception e) {
                    logger.error("Parse path error: {} {}", entry.getKey(), e);
                    continue;
                }

                Zone zone = zones.get(chainPath.getZone());
                if (zone == null) {
                    // zone not exists, bug?
                    logger.error("Zone not exists! Peer: {} Path: {}", peer, chainPath);
                    continue;
                }

                Chain chain = zone.getChain(chainPath.getChain());
                if (chain == null) {
                    // stub not exists, bug?
                    logger.error("Stub not exists! Peer: {} Path: {}", peer, chainPath);
                    continue;
                }

                if (entry.getValue().getResources() != null) {
                    for (ResourceInfo resourceInfo : entry.getValue().getResources()) {
                        Resource resource = chain.getResources().get(resourceInfo.getName());

                        if (resource == null) {
                            // resource not exists, bug?
                            logger.error("Resource not exists! Peer: {} Path: {}", peer, chainPath);
                            continue;
                        }

                        if (!resource.isTemporary()) {
                            resource.removeConnection(peer);

                            if (resource.isConnectionEmpty()) {
                                chain.getResources().remove(resourceInfo.getName());
                            }
                        }
                    }
                }

                if (removeChainConnection) {
                    chain.removeConnection(peer);
                }

                if (chain.getConnection().isEmpty()) {
                    chain.stop();
                    logger.info(
                            "Stop block header sync: {}",
                            chainPath.getZone() + "." + chainPath.getChain());

                    zone.getChains().remove(chainPath.getChain());
                }

                if (zone.getChains().isEmpty()) {
                    zones.remove(chainPath.getZone());
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

    public Map<String, ChainInfo> getAllChainsInfo(boolean ignoreRemote) {
        Map<String, ChainInfo> chains = new HashMap<String, ChainInfo>();

        lock.readLock().lock();
        try {
            for (Map.Entry<String, Zone> zoneEntry : zones.entrySet()) {
                for (Map.Entry<String, Chain> chainEntry :
                        zoneEntry.getValue().getChains().entrySet()) {
                    if (ignoreRemote && !chainEntry.getValue().hasLocalConnection) {
                        continue;
                    }

                    String chainName = PathUtils.toPureName(chainEntry.getKey());

                    chains.put(chainName, chainEntry.getValue().getChainInfo());
                }
            }
        } finally {
            lock.readLock().unlock();
        }

        return chains;
    }

    public P2PService getP2PService() {
        return p2PService;
    }

    public void setP2PService(P2PService p2PService) {
        this.p2PService = p2PService;
    }

    public StubManager getStubManager() {
        return stubManager;
    }

    public void setStubManager(StubManager stubManager) {
        this.stubManager = stubManager;
    }

    public MemoryBlockHeaderManagerFactory getResourceBlockHeaderManagerFactory() {
        return memoryBlockHeaderManagerFactory;
    }

    public void setResourceBlockHeaderManagerFactory(
            MemoryBlockHeaderManagerFactory resourceBlockHeaderManagerFactory) {
        this.memoryBlockHeaderManagerFactory = resourceBlockHeaderManagerFactory;
    }
}
