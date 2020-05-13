package com.webank.wecross.config;

import com.webank.wecross.network.p2p.P2PService;
import com.webank.wecross.network.p2p.netty.NettyBootstrap;
import com.webank.wecross.network.p2p.netty.NettyService;
import com.webank.wecross.network.p2p.netty.SeqMapper;
import com.webank.wecross.network.p2p.netty.factory.MessageCallbackFactory;
import com.webank.wecross.network.p2p.netty.factory.NettyBootstrapFactory;
import com.webank.wecross.network.p2p.netty.factory.NettyServiceFactory;
import com.webank.wecross.network.p2p.netty.factory.P2PConfig;
import com.webank.wecross.network.p2p.netty.factory.SeqMapperFactory;
import com.webank.wecross.network.p2p.netty.factory.ThreadPoolTaskExecutorFactory;
import com.webank.wecross.network.p2p.netty.message.MessageCallBack;
import com.webank.wecross.peer.PeerManager;
import com.webank.wecross.zone.ZoneManager;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class P2PServiceConfig {

    @Resource P2PConfig p2pConfig;

    @Resource PeerManager peerManager;

    @Resource ZoneManager zoneManager;

    @Bean
    public P2PService newP2PService() {
        ThreadPoolTaskExecutor threadPool =
                ThreadPoolTaskExecutorFactory.build(p2pConfig.getThreadNum(), "netty-p2p");
        SeqMapper seqMapper = SeqMapperFactory.build();
        MessageCallBack messageCallback =
                MessageCallbackFactory.build(seqMapper, peerManager, zoneManager);
        NettyBootstrap nettyBootstrap =
                NettyBootstrapFactory.build(p2pConfig, threadPool, messageCallback);
        NettyService nettyService =
                NettyServiceFactory.build(seqMapper, threadPool, nettyBootstrap);

        P2PService p2PService = new P2PService();
        p2PService.setNettyService(nettyService);
        return p2PService;
    }
}
