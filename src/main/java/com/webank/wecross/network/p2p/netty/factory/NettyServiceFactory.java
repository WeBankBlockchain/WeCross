package com.webank.wecross.network.p2p.netty.factory;

import com.webank.wecross.network.p2p.netty.NettyBootstrap;
import com.webank.wecross.network.p2p.netty.NettyService;
import com.webank.wecross.network.p2p.netty.SeqMapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class NettyServiceFactory {
    public static NettyService build(
            SeqMapper seqMapper, ThreadPoolTaskExecutor threadPool, NettyBootstrap nettyBootstrap) {

        System.out.println("Initializing P2PService ...");

        NettyService nettyService = new NettyService();
        nettyService.setThreadPool(threadPool);
        nettyService.setInitializer(nettyBootstrap);
        nettyService.setSeqMapper(seqMapper);

        return nettyService;
    }
}
