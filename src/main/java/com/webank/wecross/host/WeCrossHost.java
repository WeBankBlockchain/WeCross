package com.webank.wecross.host;

import com.webank.wecross.network.NetworkManager;
import com.webank.wecross.p2p.P2PMessage;
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

    public void start() {
        addTestResources();
        peerManager.start();

        final long timeInterval = 5000;
        Runnable runnable =
                new Runnable() {
                    public void run() {
                        while (true) {
                            try {
                                workLoop();
                                Thread.sleep(timeInterval);
                            } catch (Exception e) {
                                logger.error("Startup error: " + e);
                                System.exit(-1);
                            }
                        }
                    }
                };

        Thread thread = new Thread(runnable);
        thread.start();
    }

    public Resource getResource(Path path) throws Exception {
        return networkManager.getResource(path);
    }

    public StateResponse getState(StateRequest request) {
        return networkManager.getState(request);
    }

    public Object onRestfulPeerMessage(String method, P2PMessage msg) {
        return peerManager.onRestfulPeerMessage(method, msg);
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

    private void addTestResources() {
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

    private void workLoop() {
        peerManager.broadcastSeqRequest();
        peerManager.syncWithPeerNetworks();
    }
}
