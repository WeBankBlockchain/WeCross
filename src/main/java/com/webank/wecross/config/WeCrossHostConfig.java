package com.webank.wecross.config;

import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.netty.P2PService;
import com.webank.wecross.peer.PeerManager;
import com.webank.wecross.zone.ZoneManager;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WeCrossHostConfig {

    private Logger logger = LoggerFactory.getLogger(WeCrossHostConfig.class);

    @Resource ZoneManager zoneManager;

    @Resource P2PService p2pService;

    @Resource PeerManager peerManager;

    @Resource P2PMessageEngine p2pMessageEngine;

    @Bean
    public WeCrossHost newWeCrossHost() {
        WeCrossHost host = new WeCrossHost();
        host.setZoneManager(zoneManager);
        host.setP2pService(p2pService);
        host.setPeerManager(peerManager);

        // set the p2p engine here to avoid circular reference
        zoneManager.setP2PEngine(p2pMessageEngine);

        try {
            host.findHTLCResourcePairs();
        } catch (Exception e) {
            logger.warn("something wrong with getHTLCResourcePairs: {}", e.getLocalizedMessage());
        }

        host.start();
        return host;
    }
}
