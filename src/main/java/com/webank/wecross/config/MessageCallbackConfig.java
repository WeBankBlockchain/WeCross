package com.webank.wecross.config;

import com.webank.wecross.p2p.HeartBeatProcessor;
import com.webank.wecross.p2p.MessageType;
import com.webank.wecross.p2p.P2PConfig;
import com.webank.wecross.p2p.RequestProcessor;
import com.webank.wecross.p2p.ResponseProcessor;
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
public class MessageCallbackConfig {
    @Resource
    HeartBeatProcessor heartBeatProcessor;
    @Resource
    ResponseProcessor resourceResponseProcessor;
    @Resource
    RequestProcessor resourceRequestProcessor;

    @Resource
    PeerManager peerManager;
    @Resource
    ZoneManager zoneManager;
    @Resource
    SeqMapper seqMapper;
    
    @Bean
    public HeartBeatProcessor newHeartBeatProcessor() {
    	return new HeartBeatProcessor();
    }
    
    @Bean
    public ResponseProcessor newResourceResponseProcessor() {
    	ResponseProcessor resourceResponseProcessor = new ResponseProcessor();
    	resourceResponseProcessor.setSeqMapper(seqMapper);
    	return resourceResponseProcessor;
    }
    
    @Bean
    public RequestProcessor newResourceRequestProcessor() {
    	RequestProcessor resourceRequestProcessor = new RequestProcessor();
    	resourceRequestProcessor.setPeerManager(peerManager);
    	resourceRequestProcessor.setZoneManager(zoneManager);
    	
    	return resourceRequestProcessor;
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
}
