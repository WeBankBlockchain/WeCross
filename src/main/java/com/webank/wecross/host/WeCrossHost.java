package com.webank.wecross.host;

import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.netty.common.Node;
import com.webank.wecross.peer.PeerInfo;
import com.webank.wecross.peer.PeerManager;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.resource.TestResource;
import com.webank.wecross.stub.StateRequest;
import com.webank.wecross.stub.StateResponse;
import com.webank.wecross.zone.ZoneManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeCrossHost {

    private Logger logger = LoggerFactory.getLogger(WeCrossHost.class);

    private ZoneManager networkManager;
    private PeerManager peerManager;

    public void start() {
        /** start netty p2p service */
        try {
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

    public void setNetworkManager(ZoneManager networkManager) {
        this.networkManager = networkManager;
    }

    public ZoneManager getNetworkManager() {
        return this.networkManager;
    }

    public void setPeerManager(PeerManager peerManager) {
        this.peerManager = peerManager;
    }

    public void syncAllState() {}
}
