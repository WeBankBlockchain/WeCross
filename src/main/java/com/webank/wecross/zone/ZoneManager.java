package com.webank.wecross.zone;

import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.peer.Peer;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.resource.ResourceInfo;
import com.webank.wecross.restserver.request.ResourceRequest;
import com.webank.wecross.restserver.response.ResourceResponse;
import com.webank.wecross.stub.StateRequest;
import com.webank.wecross.stub.StateResponse;
import com.webank.wecross.stub.Stub;
import com.webank.wecross.stub.remote.RemoteResource;
import com.webank.wecross.stub.remote.RemoteStub;
import com.webank.wecross.utils.core.PathUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZoneManager {
    private Map<String, Zone> zones = new HashMap<>();
    private int seq = 1;
    private Logger logger = LoggerFactory.getLogger(ZoneManager.class);
    private P2PMessageEngine p2pEngine;
    private ReadWriteLock lock = new ReentrantReadWriteLock();

    public StateResponse getState(StateRequest request) {

        StateResponse response = new StateResponse();
        response.setSeq(seq);

        return response;
    }

    public Resource getResource(Path path) {
        lock.readLock().lock();
        try {
            Zone network = getZone(path);

            if (network != null) {
                Stub stub = network.getStub(path);

                if (stub != null) {
                    Resource resource = stub.getResource(path);

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

    public void setZones(Map<String, Zone> networks) {
        this.zones = networks;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public Map<String, Stub> getAllStubInfo(boolean ignoreLocal) {
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

                for (Map.Entry<String, Stub> stubEntry :
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

                Stub stub = zone.getStubs().get(path.getChain());
                if (stub == null) {
                    stub = new RemoteStub();
                    zone.getStubs().put(path.getChain(), stub);
                }

                Resource resource = stub.getResources().get(path.getResource());
                if (resource == null) {
                    resource =
                            stub.getResources()
                                    .put(
                                            path.getResource(),
                                            new RemoteResource(
                                                    peer, resourceInfo.getDistance(), p2pEngine));
                } else {
                    resource.getPeers().add(peer);
                }
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

                Stub stub = zone.getStub(path.getChain());
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

                if (!resource.getPeers().remove(peer)) {
                    logger.error(
                            "Remote resource doesn't contain such peer: {} path: {}", peer, path);
                    continue;
                }

                if (resource.getPeers().isEmpty()) {
                    stub.getResources().remove(path.getResource());
                }

                if (stub.getResources().isEmpty()) {
                    zone.getStubs().remove(path.getChain());
                }

                if (zone.getStubs().isEmpty()) {
                    zones.remove(path.getNetwork());
                }
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

    public ResourceResponse list(ResourceRequest request) {
        lock.readLock().lock();
        try {
            ResourceResponse resourceResponse = new ResourceResponse();
            try {
                List<Resource> resources = getAllResources(request.isIgnoreRemote());
                resourceResponse.setErrorCode(0);
                resourceResponse.setErrorMessage("");
                resourceResponse.setResources(resources);
            } catch (Exception e) {
                resourceResponse.setErrorCode(1);
                resourceResponse.setErrorMessage("Unexpected error: " + e.getMessage());
            }

            return resourceResponse;
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
}
