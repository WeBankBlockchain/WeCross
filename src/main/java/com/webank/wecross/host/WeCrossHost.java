package com.webank.wecross.host;

import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.netty.P2PService;
import com.webank.wecross.peer.Peer;
import com.webank.wecross.peer.PeerManager;
import com.webank.wecross.peer.PeerSeqMessageData;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.restserver.Versions;
import com.webank.wecross.stub.StateRequest;
import com.webank.wecross.stub.StateResponse;
import com.webank.wecross.zone.ZoneManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeCrossHost {
    private Logger logger = LoggerFactory.getLogger(WeCrossHost.class);

    private ZoneManager zoneManager;
    private PeerManager peerManager;
    private P2PService p2pService;
    
    Thread mainLoopThread;

	public void start() {
        try {
        	p2pService.start();
        	
        	// start main loop
        	mainLoopThread = new Thread(new Runnable() {
				@Override
				public void run() {
					mainLoop();
				}
        	});
        	mainLoopThread.run();
        } catch (Exception e) {
            logger.error("Startup host error: {}", e);
            System.exit(-1);
        }
    }
	
	public void mainLoop() {
		while(true) {
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
			
			for(Peer peer: peerManager.getPeerInfos().values()) {
		        logger.debug("Send peer seq, to peer:{}, seq:{}", peer, msg.getSeq());
		        zoneManager.getP2PEngine().asyncSendMessage(peer, msg, null);
			}
		}
	}

    public Resource getResource(Path path) throws Exception {
        return zoneManager.getResource(path);
    }

    public StateResponse getState(StateRequest request) {
        return zoneManager.getState(request);
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
