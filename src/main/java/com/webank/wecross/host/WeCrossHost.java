package com.webank.wecross.host;

import com.webank.wecross.chain.StateRequest;
import com.webank.wecross.chain.StateResponse;
import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.netty.P2PService;
import com.webank.wecross.peer.Peer;
import com.webank.wecross.peer.PeerManager;
import com.webank.wecross.peer.PeerSeqMessageData;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.restserver.Versions;
import com.webank.wecross.routine.htlc.AssetHTLC;
import com.webank.wecross.routine.htlc.AssetHTLCResource;
import com.webank.wecross.routine.htlc.HTLCResourcePair;
import com.webank.wecross.routine.htlc.HTLCTaskFactory;
import com.webank.wecross.routine.task.TaskManager;
import com.webank.wecross.zone.ZoneManager;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeCrossHost {
    private Logger logger = LoggerFactory.getLogger(WeCrossHost.class);

    private ZoneManager zoneManager;
    private PeerManager peerManager;
    private P2PService p2pService;
    private List<HTLCResourcePair> htlcResourcePairs = new ArrayList<>();

    Thread mainLoopThread;

    public void start() {
        /** start htlc service */
        runHTLCService();

        /** start netty p2p service */
        try {
            p2pService.start();

            // start main loop
            mainLoopThread =
                    new Thread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    mainLoop();
                                }
                            });
            mainLoopThread.start();
        } catch (Exception e) {
            logger.error("Startup host error: {}", e);
            System.exit(-1);
        }
    }

    public void mainLoop() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("Thread exception", e);
            }

            int seq = zoneManager.getSeq();

            PeerSeqMessageData peerSeqMessageData = new PeerSeqMessageData();
            peerSeqMessageData.setSeq(seq);

            P2PMessage<Object> msg = new P2PMessage<>();
            msg.newSeq();
            msg.setData(peerSeqMessageData);
            msg.setVersion(Versions.currentVersion);
            msg.setMethod("seq");

            for (Peer peer : peerManager.getPeerInfos().values()) {
                logger.debug("Send peer seq, to peer:{}, seq:{}", peer, msg.getSeq());
                zoneManager.getP2PEngine().asyncSendMessage(peer, msg, null);
            }
        }
    }

    public void runHTLCService() {
        HTLCTaskFactory htlcTaskFactory = new HTLCTaskFactory();
        TaskManager taskManager = new TaskManager(htlcTaskFactory);
        taskManager.registerTasks(htlcResourcePairs);
        taskManager.start();
    }

    public void findHTLCResourcePairs() throws Exception {
        List<Resource> resources = zoneManager.getAllResources(true);
        for (Resource resource : resources) {
            /*
            if (resource.getType().equalsIgnoreCase(WeCrossType.RESOURCE_TYPE_ASSET_HTLC_CONTRACT)
                    && resource.getDistance() == 0) {
            */
            if (AssetHTLCResource.class.isInstance(resources) && resource.getDistance() == 0) {
                AssetHTLC assetHTLC = new AssetHTLC();
                String counterpartyHTLCIpath = assetHTLC.getCounterpartyHTLCIpath(resource);
                Resource counterpartyHTLCResource =
                        zoneManager.getResource(Path.decode(counterpartyHTLCIpath));
                htlcResourcePairs.add(
                        new HTLCResourcePair(assetHTLC, resource, counterpartyHTLCResource));
            }
        }
    }

    public Resource getResource(Path path) throws Exception {
        return zoneManager.getResource(path);
    }

    public StateResponse getState(StateRequest request) {
        StateResponse response = new StateResponse();
        response.setSeq(zoneManager.getSeq());
        return response;
    }

    public void setZoneManager(ZoneManager networkManager) {
        this.zoneManager = networkManager;
    }

    public ZoneManager getZoneManager() {
        return this.zoneManager;
    }

    public PeerManager getPeerManager() {
        return peerManager;
    }

    public void setPeerManager(PeerManager peerManager) {
        this.peerManager = peerManager;
    }

    public P2PService getP2pService() {
        return p2pService;
    }

    public void setP2pService(P2PService p2pService) {
        this.p2pService = p2pService;
    }
}
