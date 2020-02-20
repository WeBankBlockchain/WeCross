package com.webank.wecross.config;

import com.webank.wecross.p2p.HeartBeatProcessor;
import com.webank.wecross.p2p.MessageType;
import com.webank.wecross.p2p.P2PConfig;
import com.webank.wecross.p2p.ResourceRequestProcessor;
import com.webank.wecross.p2p.ResourceResponseProcessor;
import com.webank.wecross.p2p.netty.NettyBootstrap;
import com.webank.wecross.p2p.netty.P2PService;
import com.webank.wecross.p2p.netty.SeqMapper;
import com.webank.wecross.p2p.netty.common.Node;
import com.webank.wecross.p2p.netty.message.MessageCallBack;
import com.webank.wecross.p2p.netty.message.processor.Processor;
import com.webank.wecross.p2p.netty.message.proto.Message;
import com.webank.wecross.peer.PeerManager;
import com.webank.wecross.zone.ZoneManager;

import io.netty.channel.ChannelHandlerContext;

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
    PeerManager peerManager;
    @Resource
    ZoneManager zoneManager;
    @Resource
    SeqMapper seqMapper;
    @Resource
    MessageCallBack messageCallBack;
    @Resource
    NettyBootstrap nettyBootstrap;
    
    @Bean
    public HeartBeatProcessor newHeartBeatProcessor() {
    	return new HeartBeatProcessor();
    }
    
    @Bean
    public ResourceResponseProcessor newResourceResponseProcessor() {
    	ResourceResponseProcessor resourceResponseProcessor = new ResourceResponseProcessor();
    	resourceResponseProcessor.setSeqMapper(seqMapper);
    	return resourceResponseProcessor;
    }
    
    @Bean
    public ResourceRequestProcessor newResourceRequestProcessor() {
    	ResourceRequestProcessor resourceRequestProcessor = new ResourceRequestProcessor();
    	resourceRequestProcessor.setPeerManager(peerManager);
    	resourceRequestProcessor.setZoneManager(zoneManager);
    	
    	return resourceRequestProcessor;
    }
    
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

        MessageCallBack callback = new MessageCallBack();
        callback.setSeqMapper(seqMapper);

        callback.setProcessor(MessageType.HEARTBEAT, heartBeatProcessor);
        callback.setProcessor(MessageType.RESOURCE_REQUEST, resourceRequestProcessor);
        callback.setProcessor(MessageType.RESOURCE_RESPONSE, resourceResponseProcessor);
        
        callback.setProcessor(MessageCallBack.ON_CONNECT, new Processor() {
			@Override
			public String name() {
				return "ConnectProcessor";
			}

			@Override
			public void process(ChannelHandlerContext ctx, Node node, Message message) {
				_peerManager.addPeerInfo(node);
			}
        	
			PeerManager _peerManager = peerManager;
        });
        
        callback.setProcessor(MessageCallBack.ON_DISCONNECT, new Processor() {

			@Override
			public String name() {
				return "DisconnectProcessor";
			}

			@Override
			public void process(ChannelHandlerContext ctx, Node node, Message message) {
				_peerManager.removePeerInfo(node);
			}
        	
			PeerManager _peerManager = peerManager;
        });

        return callback;
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
        p2pService.setThreadPool(threadPoolTaskExecutor);
        p2pService.setInitializer(nettyBootstrap);
        p2pService.setSeqMapper(seqMapper);

        return p2pService;
    }
}
