package com.webank.wecross.p2p.netty;

import com.webank.wecross.p2p.config.P2PConfig;
import com.webank.wecross.p2p.netty.channel.handler.ChannelHandlerCallBack;
import com.webank.wecross.p2p.netty.message.MessageCallBack;
import com.webank.wecross.p2p.netty.message.MessageType;
import com.webank.wecross.p2p.netty.message.processor.HeartBeatProcessor;
import com.webank.wecross.p2p.netty.message.processor.ResourceRequestProcessor;
import com.webank.wecross.p2p.netty.message.processor.ResourceResponseProcessor;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class Compoment {
    @Resource P2PConfig p2PConfig;

    @Resource SeqMapper seqMapper;

    @Resource Connections connections;

    @Resource(name = "newThreadPoolTaskExecutor")
    ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource HeartBeatProcessor heartBeatProcessor;

    @Resource ResourceRequestProcessor resourceRequestProcessor;

    @Resource ResourceResponseProcessor resourceResponseProcessor;

    @Resource(name = "newMessageCallBack")
    MessageCallBack messageCallBack;

    @Resource(name = "newChannelHandlerCallBack")
    ChannelHandlerCallBack channelHandlerCallBack;

    @Resource(name = "newInitializer")
    Initializer initializer;

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
    public ChannelHandlerCallBack newChannelHandlerCallBack() {
        ChannelHandlerCallBack callBack = new ChannelHandlerCallBack();
        callBack.setConnections(connections);
        callBack.setThreadPool(threadPoolTaskExecutor);
        callBack.setCallBack(messageCallBack);
        return callBack;
    }

    @Bean
    public Initializer newInitializer() {
        Initializer initializer = new Initializer();
        initializer.setChannelHandlerCallBack(channelHandlerCallBack);
        initializer.setConfig(p2PConfig);
        initializer.setConnections(connections);

        return initializer;
    }

    @Bean
    public P2PService newP2PService() {

        P2PService p2PService = new P2PService();
        p2PService.setThreadPool(threadPoolTaskExecutor);
        p2PService.setInitializer(initializer);
        p2PService.setConnections(connections);
        p2PService.setSeqMapper(seqMapper);
        p2PService.setChannelHandlerCallBack(channelHandlerCallBack);

        return p2PService;
    }
}
