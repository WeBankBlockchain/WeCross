package com.webank.wecross.zone;

import com.webank.wecross.config.BlockVerifierTomlConfig;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.network.p2p.P2PService;
import com.webank.wecross.peer.Peer;
import com.webank.wecross.peer.PeerManager;
import com.webank.wecross.remote.RemoteConnection;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stubmanager.MemoryBlockManager;
import com.webank.wecross.stubmanager.MemoryBlockManagerFactory;
import com.webank.wecross.stubmanager.StubManager;
import com.webank.wecross.utils.core.PathUtils;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZoneManager {
    private final Logger logger = LoggerFactory.getLogger(ZoneManager.class);
    private Map<String, Zone> zones = new LinkedHashMap<>();
    private AtomicInteger seq = new AtomicInteger(1);
    private P2PService p2PService;
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private StubManager stubManager;
    private MemoryBlockManagerFactory memoryBlockManagerFactory;
    private PeerManager peerManager;
    private BlockVerifierTomlConfig.Verifiers verifiers;

    public Chain getChain(Path path) {
        lock.readLock().lock();
        try {
            Zone zone = getZone(path);

            if (zone != null) {
                Chain chain = zone.getChain(path);

                return chain;
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Exception: " + e);
            }
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
                    Resource resource = chain.getResource(path);

                    if (resource != null) {
                        return resource;
                    } else {
                        if (create) {
                            ResourceInfo resourceInfo = new ResourceInfo();
                            resourceInfo.setName(path.getResource());

                            // not found, build default resource
                            resource = new Resource();
                            resource.setPath(path);
                            resource.setBlockManager(chain.getBlockManager());
                            resource.setDriver(chain.getDriver());
                            resource.setStubType(chain.getStubType());
                            resource.setResourceInfo(resourceInfo);

                            Connection localConnection = chain.getLocalConnection();
                            if (localConnection != null) {
                                resource.addConnection(null, localConnection);
                            } else {

                                Set<Peer> peers = chain.getPeers();
                                for (Peer peer : peers) {
                                    RemoteConnection remoteConnection = new RemoteConnection();
                                    remoteConnection.setP2PService(p2PService);
                                    remoteConnection.setPeer(peer);
                                    remoteConnection.setPath(path.toURI());
                                    remoteConnection.setProperties(
                                            chain.getChainInfo().getProperties());
                                    resource.addConnection(peer, remoteConnection);
                                }
                            }

                            resource.setTemporary(true);

                            return resource;
                        } else {
                            return null;
                        }
                    }
                }
            }

            return null;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Exception: " + e);
            }
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
            if (logger.isTraceEnabled()) {
                logger.trace("get zone: {}", name);
            }
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

    public BlockVerifierTomlConfig.Verifiers getVerifiers() {
        return verifiers;
    }

    public void setVerifiers(BlockVerifierTomlConfig.Verifiers verifiers) {
        this.verifiers = verifiers;
    }

    public boolean addRemoteChains(Peer peer, Map<String, ChainInfo> chainInfos) throws Exception {
        lock.writeLock().lock();
        boolean changed = false;
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

                Zone zone = zones.get(chainPath.getZone());
                if (zone == null) {
                    zone = new Zone();
                    zones.put(chainPath.getZone(), zone);
                }

                Chain chain = zone.getChains().get(chainPath.getChain());

                if (chain == null) {
                    Driver driver = stubManager.getStubDriver(chainInfo.getStubType());

                    chain = new Chain(chainPath.getZone(), chainInfo, driver, null);
                    MemoryBlockManager resourceBlockHeaderManager =
                            memoryBlockManagerFactory.build(chain);

                    chain.setStubType(chainInfo.getStubType());
                    chain.setBlockManager(resourceBlockHeaderManager);

                    zone.getChains().put(chainPath.getChain(), chain);
                } else {
                    // verify checksum
                    ChainInfo oldChainInfo = chain.getChainInfo();
                    if (!oldChainInfo.getChecksum().equals(chainInfo.getChecksum())) {
                        logger.error(
                                "addRemoteChains: Chain checksum is not the same: old:{}, receive:{}",
                                oldChainInfo.toString(),
                                chainInfo.toString());
                        continue;
                    }
                }

                for (ResourceInfo resourceInfo : chainInfo.getResources()) {
                    Path resourcePath = new Path();
                    resourcePath.setZone(chainPath.getZone());
                    resourcePath.setChain(chainPath.getChain());
                    resourcePath.setResource(resourceInfo.getName());

                    // did config verifiers
                    if (this.verifiers != null && this.verifiers.getVerifiers().size() > 0) {
                        BlockVerifierTomlConfig.Verifiers.BlockVerifier blockVerifier =
                                this.verifiers.getVerifiers().get(chainPath.toString());
                        // did not config this chain
                        if (blockVerifier != null) {
                            chainInfo.getProperties().put("VERIFIER", blockVerifier.toJson());
                        } else {
                            logger.warn(
                                    "Chain did not config verifier, chain: {}",
                                    chainPath.toString());
                        }
                    }
                    RemoteConnection remoteConnection = new RemoteConnection();
                    remoteConnection.setP2PService(p2PService);
                    remoteConnection.setPeer(peer);
                    remoteConnection.setPath(resourcePath.toURI());
                    remoteConnection.setProperties(chainInfo.getProperties());

                    chain.addRemoteResource(peer, resourceInfo, remoteConnection);
                }
                chain.start();
                changed = true;
            }
        } catch (WeCrossException e) {
            logger.error("Add remote resource error", e);
            throw e;
        } finally {
            lock.writeLock().unlock();
        }
        return changed;
    }

    public boolean removeRemoteChains(
            Peer peer, Map<String, ChainInfo> chains, boolean removeChainPeer) {
        lock.writeLock().lock();
        boolean changed = false; // return
        try {
            for (Map.Entry<String, ChainInfo> entry : chains.entrySet()) {
                Path chainPath;
                ChainInfo chainInfo = entry.getValue();
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

                ChainInfo oldChainInfo = chain.getChainInfo();
                if (!oldChainInfo.getChecksum().equals(chainInfo.getChecksum())) {
                    logger.error(
                            "removeRemoteChains: Chain checksum is not the same: old:{}, receive:{}",
                            oldChainInfo.toString(),
                            chainInfo.toString());
                    continue;
                }

                if (chainInfo.getResources() != null) {
                    for (ResourceInfo resourceInfo : chainInfo.getResources()) {
                        Resource resource = chain.getResource(resourceInfo.getName());

                        if (resource == null) {
                            // resource not exists, bug?
                            logger.error("Resource not exists! Peer: {} Path: {}", peer, chainPath);
                            continue;
                        }

                        if (!resource.isTemporary()) {
                            resource.removeConnection(peer);

                            if (resource.isConnectionEmpty()) {
                                chain.removeResource(resourceInfo.getName(), false);
                            }
                        }
                    }
                }

                if (removeChainPeer) {
                    chain.removePeers(peer);
                }

                if (chain.getPeers().isEmpty() && !chain.hasLocalConnection()) {
                    chain.stop();
                    zone.getChains().remove(chainPath.getChain());
                }

                changed = true;
            }
        } finally {
            lock.writeLock().unlock();
        }
        return changed;
    }

    public Map<String, Resource> getChainResources(Path chainPath) {
        Map<String, Resource> resources = new LinkedHashMap<>();
        lock.readLock().lock();
        try {
            for (Resource resourceEntry : getChain(chainPath).getResources().values()) {
                String resourceName = PathUtils.toPureName(resourceEntry.getPath().toString());
                chainPath.setResource(resourceName);
                resources.put(chainPath.toString(), resourceEntry);
            }
        } finally {
            lock.readLock().unlock();
        }
        return resources;
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
                        if (resourceEntry.getValue().hasLocalConnection() || !ignoreRemote) {
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
                    if (ignoreRemote && !chainEntry.getValue().hasLocalConnection()) {
                        continue;
                    }

                    String chainName = PathUtils.toPureName(chainEntry.getKey());

                    Path path = new Path();
                    path.setZone(zoneEntry.getKey());
                    path.setChain(chainName);

                    chains.put(path.toString(), chainEntry.getValue().getChainInfo());
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

    public MemoryBlockManagerFactory getMemoryBlockManagerFactory() {
        return memoryBlockManagerFactory;
    }

    public void setMemoryBlockManagerFactory(MemoryBlockManagerFactory memoryBlockManagerFactory) {
        this.memoryBlockManagerFactory = memoryBlockManagerFactory;
    }

    public void setPeerManager(PeerManager peerManager) {
        this.peerManager = peerManager;
    }

    public void newSeq() {
        this.seq.addAndGet(1);
    }
}
