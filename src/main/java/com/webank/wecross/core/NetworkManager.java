package com.webank.wecross.core;

import com.webank.wecross.network.Network;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.resource.URI;
import com.webank.wecross.stub.Stub;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkManager {
    private Map<String, Network> networks;
    private int seq = 1;
    private Logger logger = LoggerFactory.getLogger(NetworkManager.class);

    public StateResponse getState(StateRequest request) {
        StateResponse response = new StateResponse();
        response.setSeq(seq);

        return response;
    }

    public Resource getResource(URI uri) throws Exception {
        Network network = getNetwork(uri);

        if (network != null) {
            Stub stub = network.getStub(uri);

            if (stub != null) {
                Resource resource = stub.getResource(uri);

                return resource;
            }
        }

        return null;
    }

    public Network getNetwork(URI uri) {
        return getNetwork(uri.getNetwork());
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
}
