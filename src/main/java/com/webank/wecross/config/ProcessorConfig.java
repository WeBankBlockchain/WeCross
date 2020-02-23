package com.webank.wecross.config;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.webank.wecross.p2p.ConnectProcessor;
import com.webank.wecross.p2p.DisconnectProcessor;
import com.webank.wecross.p2p.HeartBeatProcessor;
import com.webank.wecross.p2p.RequestProcessor;
import com.webank.wecross.p2p.ResponseProcessor;
import com.webank.wecross.p2p.netty.SeqMapper;
import com.webank.wecross.peer.PeerManager;
import com.webank.wecross.zone.ZoneManager;

@Configuration
public class ProcessorConfig {
	@Resource
    ZoneManager zoneManager;
    @Resource
    SeqMapper seqMapper;
	@Resource
    PeerManager peerManager;
	
	@Bean
    public HeartBeatProcessor newHeartBeatProcessor() {
    	return new HeartBeatProcessor();
    }
    
    @Bean
    public ResponseProcessor newResponseProcessor() {
    	ResponseProcessor resourceResponseProcessor = new ResponseProcessor();
    	resourceResponseProcessor.setSeqMapper(seqMapper);
    	return resourceResponseProcessor;
    }
    
    @Bean
    public RequestProcessor newRequestProcessor() {
    	RequestProcessor resourceRequestProcessor = new RequestProcessor();
    	resourceRequestProcessor.setPeerManager(peerManager);
    	resourceRequestProcessor.setZoneManager(zoneManager);
    	
    	return resourceRequestProcessor;
    }
    
    @Bean
    public ConnectProcessor newConnectProcessor() {
    	ConnectProcessor connectProcessor = new ConnectProcessor();
    	connectProcessor.setPeerManager(peerManager);
    	
    	return connectProcessor;
    }
    
    @Bean DisconnectProcessor newDisconnectProcessor() {
    	DisconnectProcessor disconnectProcessor = new DisconnectProcessor();
    	disconnectProcessor.setPeerManager(peerManager);
    	
    	return disconnectProcessor;
    }
}
