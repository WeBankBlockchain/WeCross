package com.webank.wecross.config;

import com.webank.wecross.account.AccountManager;
import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.p2p.MessageType;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.RequestProcessor;
import com.webank.wecross.p2p.netty.P2PService;
import com.webank.wecross.peer.PeerManager;
import com.webank.wecross.routine.htlc.HTLCManager;
import com.webank.wecross.zone.ZoneManager;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WeCrossHostConfig {

    private Logger logger = LoggerFactory.getLogger(WeCrossHostConfig.class);

    @Resource private ZoneManager zoneManager;

    @Resource private P2PService p2pService;

    @Resource private PeerManager peerManager;

    @Resource private P2PMessageEngine p2pMessageEngine;

    @Resource private HTLCManager htlcManager;

    @Resource private AccountManager accountManager;

    @Bean
    public WeCrossHost newWeCrossHost() {
        WeCrossHost host = new WeCrossHost();
        host.setZoneManager(zoneManager);
        host.setP2pService(p2pService);
        host.setPeerManager(peerManager);
        host.setAccountManager(accountManager);
        host.setHtlcManager(htlcManager);

        // set the p2p engine here to avoid circular reference
        zoneManager.setP2PEngine(p2pMessageEngine);
        RequestProcessor processor =
                (RequestProcessor)
                        p2pService
                                .getInitializer()
                                .getMessageCallBack()
                                .getProcessor(MessageType.RESOURCE_REQUEST);
        processor.setP2pEngine(p2pMessageEngine);

        host.start();
        return host;
    }
}
