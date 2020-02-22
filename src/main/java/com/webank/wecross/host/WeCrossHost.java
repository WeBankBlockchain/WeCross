package com.webank.wecross.host;

import com.webank.wecross.p2p.netty.P2PService;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.StateRequest;
import com.webank.wecross.stub.StateResponse;
import com.webank.wecross.zone.ZoneManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeCrossHost {

    private Logger logger = LoggerFactory.getLogger(WeCrossHost.class);

    private ZoneManager zoneManager;
    private P2PService p2pService;

	public void start() {
        try {
        	p2pService.start();
        } catch (Exception e) {
            logger.error("Startup host error: {}", e);
            System.exit(-1);
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
    
    public P2PService getP2pService() {
		return p2pService;
	}

	public void setP2pService(P2PService p2pService) {
		this.p2pService = p2pService;
	}

    public void syncAllState() {}
}
