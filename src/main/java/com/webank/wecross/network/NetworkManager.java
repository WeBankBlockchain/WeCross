package com.webank.wecross.network;

import com.webank.wecross.core.PathUtils;
import com.webank.wecross.host.Peer;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.StateRequest;
import com.webank.wecross.stub.StateResponse;
import com.webank.wecross.stub.Stub;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkManager {

    private Map<String, Network> networks = new HashMap<>();
    private int seq = 1;
    private Logger logger = LoggerFactory.getLogger(NetworkManager.class);

    public StateResponse getState(StateRequest request) {
        StateResponse response = new StateResponse();
        response.setSeq(seq);

        return response;
    }

    public Resource getResource(Path path) throws Exception {
        Network network = getNetwork(path);

        if (network != null) {
            Stub stub = network.getStub(path);

            if (stub != null) {
                Resource resource = stub.getResource(path);

                return resource;
            }
        }

        return null;
    }

    public void addResource(Resource resource) throws Exception {
        String networkName = resource.getPath().getNetwork();
        networks.putIfAbsent(networkName, new Network());
        networks.get(networkName).addResource(resource);
    }

    public void removeResource(Path path) throws Exception {
        Network network = getNetwork(path);
        network.removeResource(path);
        if (network.isEmpty()) {
            networks.remove(path.getNetwork());
        }
    }

    public Network getNetwork(Path path) {
        return getNetwork(path.getNetwork());
    }

    public Network getNetwork(String name) {
        logger.trace("get network: {}", name);
        Network network = networks.get(name);
        return network;
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

    public Set<String> getAllNetworkStubResourceName(boolean ignoreRemote) {
        Set<String> ret = new HashSet<>();

        for (Map.Entry<String, Network> entry : networks.entrySet()) {
            String networkName = PathUtils.toPureName(entry.getKey());
            Set<String> allStubResourceName = entry.getValue().getAllStubResourceName(ignoreRemote);

            for (String stubResourceName : allStubResourceName) {
                ret.add(networkName + "." + stubResourceName);
            }
        }
        return ret;
    }

    public Set<Path> getAllNetworkStubResourcePath(boolean ignoreRemote) throws Exception {
        Set<String> resourcesString = getAllNetworkStubResourceName(ignoreRemote);
        Set<Path> ret = new HashSet<>();
        for (String str : resourcesString) {
            ret.add(Path.decode(str));
        }
        return ret;
    }

    public void updateActivePeerNetwork(Set<Peer> peers) {
        //        Set<>
    }
}
