package com.webank.wecross.zone;

import com.webank.wecross.chain.Chain;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.peer.Peer;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.resource.ResourceInfo;
import com.webank.wecross.stub.StubManager;
import com.webank.wecross.stub.remote.RemoteConnection;
import com.webank.wecross.utils.core.PathUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public Resource getResource(Path path) {
        lock.readLock().lock();
        try {
            Zone network = getZone(path);

            if (network != null) {
                Chain stub = network.getStub(path);

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
            logger.trace("get network: {}", name);
            Zone network = zones.get(name);
            return network;
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

    public Map<String, Chain> getAllStubInfo(boolean ignoreLocal) {
        lock.readLock().lock();
        /*
        try {
        }
        */
        return null;
    }

    public Map<String, ResourceInfo> getAllResourceInfo(boolean ignoreRemote) {
        lock.readLock().lock();
        try {
            Set<Path> resourcePaths = getAllNetworkStubResourcePath(ignoreRemote);
            Map<String, ResourceInfo> ret = new HashMap<>();

            for (Path path : resourcePaths) {
                try {
                    Resource resource = getResource(path);
                    ResourceInfo info = new ResourceInfo();
                    info.setPath(path.toString());
                    info.setDistance(resource.getDistance());
                    info.setChecksum(resource.getChecksum());
                    info.setStubType(resource.getType());
                    ret.put(path.toString(), info);
                } catch (Exception e) {
                    logger.debug("Jump exception resources: " + path.toString());
                    continue;
                }
            }

            return ret;
        } catch (Exception e) {
            logger.debug("Exception: " + e);
        } finally {
            lock.readLock().unlock();
        }
        return null;
    }

    public Set<String> getAllResourceName(boolean ignoreRemote) {
        lock.readLock().lock();
        try {
            Set<String> ret = new HashSet<>();

            for (Map.Entry<String, Zone> zoneEntry : zones.entrySet()) {
                String networkName = PathUtils.toPureName(zoneEntry.getKey());

                for (Map.Entry<String, Chain> stubEntry :
                        zoneEntry.getValue().getStubs().entrySet()) {
                    String stubName = PathUtils.toPureName(stubEntry.getKey());

                    for (Map.Entry<String, Resource> resourceEntry :
                            stubEntry.getValue().getResources().entrySet()) {
                        if (resourceEntry.getValue().getDistance() == 0 || !ignoreRemote) {
                            String resourceName = PathUtils.toPureName(resourceEntry.getKey());
                            ret.add(networkName + "." + stubName + "." + resourceName);
                        }
                    }
                }
            }
            return ret;
        } finally {
            lock.readLock().unlock();
        }
    }

    public Set<Path> getAllNetworkStubResourcePath(boolean ignoreRemote) {
        lock.readLock().lock();
        try {
            Set<String> resourcesString = getAllResourceName(ignoreRemote);
            Set<Path> ret = new HashSet<>();
            for (String str : resourcesString) {
                ret.add(Path.decode(str));
            }
            return ret;
        } catch (Exception e) {
            logger.debug("Exception: " + e);
        } finally {
            lock.readLock().unlock();
        }
        return null;
    }

    public void addRemoteResources(Peer peer, Set<ResourceInfo> resources) {
        lock.writeLock().lock();
        try {
            for (ResourceInfo resourceInfo : resources) {
                Path path;
                try {
                    path = Path.decode(resourceInfo.getPath());
                } catch (Exception e) {
                    logger.error("Parse path error: {} {}", resourceInfo.getPath(), e);
                    continue;
                }

                Zone zone = zones.get(path.getNetwork());
                if (zone == null) {
                    zone = new Zone();
                    zones.put(path.getNetwork(), zone);
                }

                Chain chain = zone.getStubs().get(path.getChain());
                if (chain == null) {
                    chain = new Chain();
                    zone.getStubs().put(path.getChain(), chain);
                }

                RemoteConnection remoteConnection = new RemoteConnection();
                remoteConnection.setP2pEngine(p2pEngine);
                remoteConnection.setPeer(peer);
                remoteConnection.setPath(path.toURI());
                Resource resource = chain.getResources().get(path.getResource());
                if (resource == null) {
                    resource = new Resource();
                    resource.setDriver(
                            stubManager.getStubFactory(resourceInfo.getStubType()).newDriver());
                    resource.setDistance(resourceInfo.getDistance());
                    chain.getResources().put(path.getResource(), resource);
                }

                resource.addConnection(peer, remoteConnection);

                seq.addAndGet(1);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeRemoteResources(Peer peer, Set<ResourceInfo> resources) {
        lock.writeLock().lock();
        try {
            for (ResourceInfo resourceInfo : resources) {
                Path path;
                try {
                    path = Path.decode(resourceInfo.getPath());
                } catch (Exception e) {
                    logger.error("Parse path error: {} {}", resourceInfo.getPath(), e);
                    continue;
                }

                Zone zone = zones.get(path.getNetwork());
                if (zone == null) {
                    // zone not exists, bug?
                    logger.error("Zone not exists! Peer: {} Path: {}", peer, path);
                    continue;
                }

                Chain stub = zone.getStub(path.getChain());
                if (stub == null) {
                    // stub not exists, bug?
                    logger.error("Stub not exists! Peer: {} Path: {}", peer, path);
                    continue;
                }

                Resource resource = stub.getResources().get(path.getResource());

                if (resource == null) {
                    // resource not exists, bug?
                    logger.error("Resource not exists! Peer: {} Path: {}", peer, path);
                    continue;
                }

                resource.removeConnection(peer);

                if (resource.isConnectionEmpty()) {
                    stub.getResources().remove(path.getResource());
                }

                if (stub.getResources().isEmpty()) {
                    zone.getStubs().remove(path.getChain());
                }

                if (zone.getStubs().isEmpty()) {
                    zones.remove(path.getNetwork());
                }

                seq.addAndGet(1);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<Resource> getAllResources(boolean ignoreRemote) throws Exception {
        lock.readLock().lock();
        try {
            List<Resource> resourcesList = new ArrayList<>();

            Set<Path> pathSet = getAllNetworkStubResourcePath(ignoreRemote);
            for (Path path : pathSet) {
                Resource resource = getResource(path);
                resourcesList.add(resource);
            }

            return resourcesList;
        } finally {
            lock.readLock().unlock();
        }
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
}
