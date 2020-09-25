package com.webank.wecross.config;

import com.webank.wecross.account.AccountSyncManager;
import com.webank.wecross.network.p2p.P2PProcessor;
import com.webank.wecross.network.p2p.P2PService;
import com.webank.wecross.peer.PeerManager;
import com.webank.wecross.routine.RoutineManager;
import com.webank.wecross.zone.ZoneManager;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class P2PProcessorConfig {

    @Resource P2PService p2PService;

    @Resource PeerManager peerManager;

    @Resource AccountSyncManager accountSyncManager;

    @Resource ZoneManager zoneManager;

    @Resource RoutineManager routineManager;

    @Bean
    public P2PProcessor newP2PProcessor() {
        P2PProcessor p2PProcessor = new P2PProcessor();
        p2PProcessor.setP2PService(p2PService);
        p2PProcessor.setPeerManager(peerManager);
        p2PProcessor.setAccountSyncManager(accountSyncManager);
        p2PProcessor.setZoneManager(zoneManager);
        p2PProcessor.setRoutineManager(routineManager);
        return p2PProcessor;
    }
}
