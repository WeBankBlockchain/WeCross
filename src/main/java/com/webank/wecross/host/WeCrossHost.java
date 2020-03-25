package com.webank.wecross.host;

import com.webank.wecross.account.AccountManager;
import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.netty.P2PService;
import com.webank.wecross.peer.Peer;
import com.webank.wecross.peer.PeerManager;
import com.webank.wecross.peer.PeerSeqMessageData;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.restserver.Versions;
import com.webank.wecross.restserver.request.StateRequest;
import com.webank.wecross.restserver.response.StateResponse;
import com.webank.wecross.routine.RoutineManager;
import com.webank.wecross.routine.htlc.AssetHTLC;
import com.webank.wecross.routine.htlc.HTLC;
import com.webank.wecross.routine.htlc.HTLCResource;
import com.webank.wecross.routine.htlc.HTLCResourcePair;
import com.webank.wecross.routine.htlc.HTLCTaskFactory;
import com.webank.wecross.routine.htlc.HTLCTaskInfo;
import com.webank.wecross.routine.task.TaskManager;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Path;
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
    private AccountManager accountManager;
    private RoutineManager routineManager;

    Thread mainLoopThread;

    public void init() throws Exception {
        initHTLCResourcePairs();
    }

    public void start() {
        try {
            check();
            init();
            /** start htlc service */
            runHTLCService();

            /** start netty p2p service */
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

            logger.info("WeCross router is running");
        }
    }

    public void check() throws Exception {
        if (zoneManager == null) {
            throw new Exception("zoneManager is null");
        }
        if (peerManager == null) {
            throw new Exception("peerManager is null");
        }
        if (p2pService == null) {
            throw new Exception("p2pService is null");
        }
        if (accountManager == null) {
            throw new Exception("accountManager is null");
        }
    }

    public void runHTLCService() {
        try {
            if (routineManager.getHtlcManager() != null) {
                HTLCTaskFactory htlcTaskFactory = new HTLCTaskFactory();
                TaskManager taskManager = new TaskManager(htlcTaskFactory);
                taskManager.registerTasks(initHTLCResourcePairs());
                taskManager.start();
            }
        } catch (Exception e) {
            logger.error(
                    "something wrong with runHTLCService: {}, exception: {}", e.getMessage(), e);
        }
    }

    public List<HTLCResourcePair> initHTLCResourcePairs() throws Exception {
        List<HTLCResourcePair> htlcResourcePairs = new ArrayList<>();
        List<HTLCTaskInfo> htlcTaskInfos = routineManager.getHtlcManager().getHtlcTaskInfos();
        for (HTLCTaskInfo htlcTaskInfo : htlcTaskInfos) {
            String selfPath = htlcTaskInfo.getSelfPath();
            String counterpartyPath = htlcTaskInfo.getCounterpartyPath();
            Resource selfResource = zoneManager.getResource(Path.decode(selfPath));
            Resource counterpartyResource = zoneManager.getResource(Path.decode(counterpartyPath));
            if (selfResource == null) {
                throw new Exception("htlc resource: " + selfPath + " not found");
            }
            if (counterpartyResource == null) {
                throw new Exception("htlc resource: " + counterpartyResource + " not found");
            }
            HTLCResource selfHTLCResource = new HTLCResource(selfResource);
            Account selfAccount = accountManager.getAccount(htlcTaskInfo.getSelfAccount());
            selfHTLCResource.setAccount(selfAccount);
            selfHTLCResource.setPath(selfPath);
            HTLCResource counterpartyHTLCResource = new HTLCResource(counterpartyResource);
            Account counterpartyAccount =
                    accountManager.getAccount(htlcTaskInfo.getCounterpartyAccount());
            counterpartyHTLCResource.setAccount(counterpartyAccount);
            counterpartyHTLCResource.setPath(counterpartyPath);
            HTLC assetHTLC = new AssetHTLC();
            htlcResourcePairs.add(
                    new HTLCResourcePair(assetHTLC, selfHTLCResource, counterpartyHTLCResource));
        }
        return htlcResourcePairs;
    }

    public Resource getResource(Path path) throws Exception {
        return zoneManager.getResource(path);
    }

    public StateResponse getState(StateRequest request) {
        StateResponse response = new StateResponse();
        response.setSeq(zoneManager.getSeq());
        return response;
    }

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public void setAccountManager(AccountManager accountManager) {
        this.accountManager = accountManager;
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

    public RoutineManager getRoutineManager() {
        return routineManager;
    }

    public void setRoutineManager(RoutineManager routineManager) {
        this.routineManager = routineManager;
    }
}
