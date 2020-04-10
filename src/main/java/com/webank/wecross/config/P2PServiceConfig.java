package com.webank.wecross.config;

import com.webank.wecross.p2p.netty.NettyBootstrap;
import com.webank.wecross.p2p.netty.P2PService;
import com.webank.wecross.p2p.netty.SeqMapper;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class P2PServiceConfig {
    @Resource ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource NettyBootstrap nettyBootstrap;

    @Resource SeqMapper seqMapper;

    @Bean
    public ThreadPoolTaskExecutor newThreadPoolTaskExecutor() {
        System.out.println("Initializing ThreadPoolTaskExecutor ...");
        // init default thread pool

        final int threadNum = 8;
        ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();
        threadPool.setCorePoolSize(threadNum);
        threadPool.setMaxPoolSize(threadNum);
        threadPool.setQueueCapacity(1000);
        threadPool.setThreadNamePrefix("netty-p2p");
        threadPool.initialize();
        return threadPool;
    }

    @Bean
    public P2PService newP2PService() {
        System.out.println("Initializing P2PService ...");

        P2PService p2pService = new P2PService();
        p2pService.setThreadPool(threadPoolTaskExecutor);
        p2pService.setInitializer(nettyBootstrap);
        p2pService.setSeqMapper(seqMapper);

        return p2pService;
    }
}
