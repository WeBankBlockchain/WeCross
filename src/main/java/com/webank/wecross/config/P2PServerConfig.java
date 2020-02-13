package com.webank.wecross.config;

import com.webank.wecross.p2p.HeartBeatProcessor;
import com.webank.wecross.p2p.MessageType;
import com.webank.wecross.p2p.P2PConfig;
import com.webank.wecross.p2p.ResourceRequestProcessor;
import com.webank.wecross.p2p.ResourceResponseProcessor;
import com.webank.wecross.p2p.netty.NettyBootstrap;
import com.webank.wecross.p2p.netty.P2PService;
import com.webank.wecross.p2p.netty.SeqMapper;
import com.webank.wecross.p2p.netty.message.MessageCallBack;

import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class P2PServerConfig {
    @Resource
    ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource
    HeartBeatProcessor heartBeatProcessor;
    @Resource
    ResourceResponseProcessor resourceResponseProcessor;
    @Resource
    ResourceRequestProcessor resourceRequestProcessor;

    @Resource
    P2PConfig p2PConfig;
    @Resource
    SeqMapper seqMapper;
    @Resource
    MessageCallBack messageCallBack;
    @Resource
    NettyBootstrap nettyBootstrap; 
    
    @Bean
    public ThreadPoolTaskExecutor newThreadPoolTaskExecutor() {
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
    public MessageCallBack newMessageCallBack() {

        MessageCallBack callBack = new MessageCallBack();
        callBack.setSeqMapper(seqMapper);

        callBack.setProcessor(MessageType.HEARTBEAT, heartBeatProcessor);
        callBack.setProcessor(MessageType.RESOURCE_REQUEST, resourceRequestProcessor);
        callBack.setProcessor(MessageType.RESOURCE_RESPONSE, resourceResponseProcessor);

        return callBack;
    }

    @Bean
    public NettyBootstrap newNettyBootstrap() {
        NettyBootstrap bootstrap = new NettyBootstrap();
        bootstrap.setConfig(p2PConfig);
        bootstrap.setMessageCallBack(messageCallBack);

        return bootstrap;
    }

    @Bean
    public P2PService newP2PService() {
        P2PService p2pService = new P2PService();
        // p2PService.setThreadPool(threadPoolTaskExecutor);
        p2pService.setInitializer(nettyBootstrap);
        p2pService.setSeqMapper(seqMapper);
        resourceResponseProcessor.setSeqMapper(seqMapper);

        return p2pService;
    }
}
