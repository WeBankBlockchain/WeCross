package com.webank.wecross.network;

import com.webank.wecross.core.PathUtils;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.netty.common.Peer;
import com.webank.wecross.peer.PeerInfo;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.resource.ResourceInfo;
import com.webank.wecross.restserver.request.ResourceRequest;
import com.webank.wecross.restserver.response.ResourceResponse;
import com.webank.wecross.stub.StateRequest;
import com.webank.wecross.stub.StateResponse;
import com.webank.wecross.stub.Stub;
import com.webank.wecross.stub.remote.RemoteResource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkManager {

    private Map<String, Network> networks = new HashMap<>();
    private int seq = 1;
    private Logger logger = LoggerFactory.getLogger(NetworkManager.class);
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
            Network network = getNetwork(path);

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

    public void addResource(Resource resource) throws Exception {
        lock.writeLock().lock();
        try {
            logger.trace("Add resource path:{}", resource.getPath());
            String networkName = resource.getPath().getNetwork();
            networks.putIfAbsent(networkName, new Network());
            networks.get(networkName).addResource(resource);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeResource(Path path, boolean ignoreLocal) throws Exception {
        lock.writeLock().lock();
        try {
            logger.trace("Remove resource ignore:{} path:{}", ignoreLocal, path);
            Network network = getNetwork(path);
            if (network != null) {
                network.removeResource(path, ignoreLocal);

                if (network.isEmpty()) {
                    networks.remove(path.getNetwork());
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeResource(Path path) throws Exception {
        lock.writeLock().lock();
        try {
            removeResource(path, false);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Network getNetwork(Path path) {
        lock.readLock().lock();
        try {
            return getNetwork(path.getNetwork());
        } finally {
            lock.readLock().unlock();
        }
    }

    public Network getNetwork(String name) {
        lock.readLock().lock();
        try {
            logger.trace("get network: {}", name);
            Network network = networks.get(name);
            return network;
        } finally {
            lock.readLock().unlock();
        }
    }

    public Map<String, Network> getNetworks() {
        return networks;
    }

    public void setNetworks(Map<String, Network> networks) {
        this.networks = networks;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public Map<String, ResourceInfo> getAllNetworkStubResourceInfo(boolean ignoreRemote) {

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

    public Set<String> getAllNetworkStubResourceName(boolean ignoreRemote) {
        lock.readLock().lock();
        try {
            Set<String> ret = new HashSet<>();

            for (Map.Entry<String, Network> entry : networks.entrySet()) {
                String networkName = PathUtils.toPureName(entry.getKey());
                Set<String> allStubResourceName =
                        entry.getValue().getAllStubResourceName(ignoreRemote);

                if (allStubResourceName == null) {
                    return null;
                }

                for (String stubResourceName : allStubResourceName) {
                    ret.add(networkName + "." + stubResourceName);
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
            Set<String> resourcesString = getAllNetworkStubResourceName(ignoreRemote);
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

    public Set<String> getInvalidResources(Set<PeerInfo> peerInfos) {
        class Path2Checksum2PeerInfos {
            Map<String, Map<String, Set<PeerInfo>>> path2Checksum2PeerInfos = new HashMap<>();

            public void update(String path, String checksum, PeerInfo peerInfo) {
                Map<String, Set<PeerInfo>> theChecksum2PeerInfos =
                        path2Checksum2PeerInfos.get(path);
                if (theChecksum2PeerInfos == null) {
                    theChecksum2PeerInfos = new HashMap<>();
                }

                Set<PeerInfo> thePeerInfos = theChecksum2PeerInfos.get(checksum);
                if (thePeerInfos == null) {
                    thePeerInfos = new HashSet<>();
                }

                thePeerInfos.add(peerInfo);

                theChecksum2PeerInfos.putIfAbsent(checksum, thePeerInfos);
                path2Checksum2PeerInfos.putIfAbsent(path, theChecksum2PeerInfos);
            }

            public Set<Map.Entry<String, Map<String, Set<PeerInfo>>>> entrySet() {
                return path2Checksum2PeerInfos.entrySet();
            }
        }

        // Generate a map with: path -> checksum -> peerInfo set
        Path2Checksum2PeerInfos path2Checksum2PeerInfos = new Path2Checksum2PeerInfos();
        for (PeerInfo peerInfo : peerInfos) {
            for (ResourceInfo resourceInfo : peerInfo.getResourceInfos()) {
                String path = resourceInfo.getPath();
                String checksum = resourceInfo.getChecksum();
                path2Checksum2PeerInfos.update(path, checksum, peerInfo);
            }
        }
        // add my local resource info
        Set<Path> localPaths = getAllNetworkStubResourcePath(true);
        for (Path path : localPaths) {
            Resource localResource = getResource(path);
            path2Checksum2PeerInfos.update(
                    path.toString(), localResource.getChecksum(), new PeerInfo(new Peer("myself")));
        }

        Set<String> ret = new HashSet<>();
        for (Map.Entry<String, Map<String, Set<PeerInfo>>> entry :
                path2Checksum2PeerInfos.entrySet()) {
            if (entry.getValue().size() > 1) {
                // same path has not unique checksum
                ret.add(entry.getKey());

                String warningContent =
                        "Receive same path with diffrent checksum, path: " + entry.getKey() + " [";
                for (Map.Entry<String, Set<PeerInfo>> errorEntry : entry.getValue().entrySet()) {
                    for (PeerInfo errorPeerInfo : errorEntry.getValue()) {
                        warningContent +=
                                "{checksum: "
                                        + errorEntry.getKey()
                                        + ", peer: "
                                        + errorPeerInfo.toString()
                                        + "},";
                    }
                }
                warningContent += "]";
                logger.warn(warningContent);
            }
        }

        return ret;
    }

    public Map<String, Pair<String,Set<Peer>>> getResource2Peers(Set<PeerInfo> peerInfos) {
        Set<String> invalidResources = getInvalidResources(peerInfos);

        Map<String, Pair<String,Set<Peer>>> resource2Peers = new HashMap<>();

        for (PeerInfo peerInfo : peerInfos) {
            for (ResourceInfo info : peerInfo.getResourceInfos()) {
                if (invalidResources.contains(info.getPath())) {
                    // ignore invalid resources
                    continue;
                }

                Pair<String,Set<Peer>> checkSumAndPeers = resource2Peers.get(info.getPath());
                if (checkSumAndPeers == null){
                    checkSumAndPeers = new Pair<>(info.getChecksum(), new HashSet<>());
                }

                Set<Peer> theResourcePeers = checkSumAndPeers.getValue();

                theResourcePeers.add(peerInfo.getPeer());
           
                resource2Peers.put(info.getPath(), checkSumAndPeers); // Replace
            }
        }
        return resource2Peers;
    }

    public void updateActivePeerNetwork(Set<PeerInfo> peerInfos) {
        lock.writeLock().lock();
        try {
            Map<String, Pair<String,Set<Peer>>> resource2Peers = getResource2Peers(peerInfos);

            Set<String> currentResources = getAllNetworkStubResourceName(false);
            logger.debug("Old resources:{}", currentResources);

            Set<String> resources2Add = new HashSet<>(resource2Peers.keySet());
            resources2Add.removeAll(currentResources);

            Set<String> resources2Remove = new HashSet<>(currentResources);
            resources2Remove.removeAll(resource2Peers.keySet());

            Set<String> resources2Update = new HashSet<>(currentResources);
            resources2Update.removeAll(resources2Remove);

            // Delete inactive remote resources
            logger.debug("Remove inactive remote resources " + resources2Remove);
            for (String resource : resources2Remove) {
                try {
                    removeResource(Path.decode(resource), true);
                } catch (Exception e) {
                    logger.error(
                            "Remove resource exception: resource:{}, exception:{}", resource, e);
                }
            }

            // Add new remote resources
            logger.debug("Add new remote resources " + resources2Add);
            for (String resource : resources2Add) {
                try {
                    Pair<String, Set<Peer>> checksumAndPeers = resource2Peers.get(resource);
                    String checksum = checksumAndPeers.getKey();
                    Set<Peer> newPeers = checksumAndPeers.getValue();
                    Resource newResource = new RemoteResource(newPeers, 1, p2pEngine);
                    ((RemoteResource) newResource).setChecksum(checksum);
                    newResource.setPath(Path.decode(resource));
                    addResource(newResource);
                } catch (Exception e) {
                    logger.error("Add resource exception: resource:{}, exception:{}", resource, e);
                }
            }

            // Update peer to resources
            logger.debug("Update remote resources " + resources2Update);
            for (String resource : resources2Update) {
                try {
                    Pair<String, Set<Peer>> checksumAndPeers = resource2Peers.get(resource);
                    String checksum = checksumAndPeers.getKey();
                    Set<Peer> newPeers = checksumAndPeers.getValue();
                    Resource resource2Update = getResource(Path.decode(resource));
                    resource2Update.setPeers(newPeers);

                    if (resource2Update.getDistance() > 1) {
                        ((RemoteResource) resource2Update).setChecksum(checksum);
                    }

                } catch (Exception e) {
                    logger.error(
                            "Update remote resources exception: resource:{}, exception:{}",
                            resource,
                            e);
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

    public void setP2pEngine(P2PMessageEngine p2pEngine) {
        this.p2pEngine = p2pEngine;
    }
}
