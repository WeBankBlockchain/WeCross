package com.webank.wecross.host;

import com.webank.wecross.network.NetworkManager;
import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.netty.common.Node;
import com.webank.wecross.peer.PeerInfo;
import com.webank.wecross.peer.PeerManager;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.resource.TestResource;
import com.webank.wecross.stub.StateRequest;
import com.webank.wecross.stub.StateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeCrossHost {

    private Logger logger = LoggerFactory.getLogger(WeCrossHost.class);

    private NetworkManager networkManager;
    private PeerManager peerManager;

    private boolean enableTestResource = false;

    public void start() {
        /** start netty p2p service */
        try {
            if (enableTestResource) {
                addTestResources();
            }
            // start p2p first
            peerManager.getP2PService().start();
            // start peer manager
            peerManager.start();
        } catch (Exception e) {
            logger.error("Startup host error: {}", e);
            System.exit(-1);
        }
    }

    public Resource getResource(Path path) throws Exception {
        return networkManager.getResource(path);
    }

    public StateResponse getState(StateRequest request) {
        return networkManager.getState(request);
    }

    public Object onRestfulPeerMessage(String method, P2PMessage msg) {
        return peerManager.onRestfulPeerMessage(new PeerInfo(new Node()), method, msg);
    }

    public void setNetworkManager(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    public NetworkManager getNetworkManager() {
        return this.networkManager;
    }

    public void setPeerManager(PeerManager peerManager) {
        this.peerManager = peerManager;
    }

    public void syncAllState() {}

    public void addTestResources() {
        try {
            logger.info("Add test resource");
            Path path = Path.decode("test-network.test-stub.test-resource");
            Resource resource = new TestResource();
            resource.setPath(path);
            networkManager.addResource(resource);
        } catch (Exception e) {
            logger.warn("Add test resource exception " + e);
        }
    }

    public void setEnableTestResource(boolean enableTestResource) {
        this.enableTestResource = enableTestResource;
    }
}
