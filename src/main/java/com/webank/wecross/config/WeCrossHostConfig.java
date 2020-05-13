package com.webank.wecross.config;

import com.webank.wecross.account.AccountManager;
import com.webank.wecross.common.BCManager;
import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.network.p2p.P2PProcessor;
import com.webank.wecross.network.p2p.P2PService;
import com.webank.wecross.peer.PeerManager;
import com.webank.wecross.routine.RoutineManager;
import com.webank.wecross.zone.ZoneManager;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WeCrossHostConfig {

    // Task layer
    @Resource private P2PProcessor p2PProcessor;

    // Data layer
    @Resource private ZoneManager zoneManager;

    @Resource private PeerManager peerManager;

    @Resource private RoutineManager routineManager;

    @Resource private AccountManager accountManager;

    @Resource private BCManager bcManager;

    // Network layer
    @Resource private P2PService p2PService;

    @Bean
    public WeCrossHost newWeCrossHost() {
        System.out.println("Initializing WeCrossHost ...");

        WeCrossHost host = new WeCrossHost();
        host.setZoneManager(zoneManager);
        host.setPeerManager(peerManager);
        host.setAccountManager(accountManager);
        host.setRoutineManager(routineManager);
        host.setP2PService(p2PService);

        // set the p2p engine here to avoid circular reference
        zoneManager.setP2PService(p2PService);

        // Service layer set processor
        p2PService.setNetworkProcessor(p2PProcessor);

        host.start();
        return host;
    }
}
