package com.webank.wecross.config;

import com.webank.wecross.p2p.ConnectProcessor;
import com.webank.wecross.p2p.DisconnectProcessor;
import com.webank.wecross.p2p.HeartBeatProcessor;
import com.webank.wecross.p2p.MessageType;
import com.webank.wecross.p2p.RequestProcessor;
import com.webank.wecross.p2p.ResponseProcessor;
import com.webank.wecross.p2p.netty.SeqMapper;
import com.webank.wecross.p2p.netty.message.MessageCallBack;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageCallbackConfig {
    @Resource HeartBeatProcessor heartBeatProcessor;

    @Resource ResponseProcessor resourceResponseProcessor;

    @Resource RequestProcessor resourceRequestProcessor;

    @Resource ConnectProcessor connectProcessor;

    @Resource DisconnectProcessor disconnectProcessor;

    @Resource SeqMapper seqMapper;

    @Bean
    public MessageCallBack newMessageCallBack() {

        MessageCallBack callback = new MessageCallBack();
        callback.setSeqMapper(seqMapper);

        callback.setProcessor(MessageType.HEARTBEAT, heartBeatProcessor);
        callback.setProcessor(MessageType.RESOURCE_REQUEST, resourceRequestProcessor);
        callback.setProcessor(MessageType.RESOURCE_RESPONSE, resourceResponseProcessor);
        callback.setProcessor(MessageCallBack.ON_CONNECT, connectProcessor);
        callback.setProcessor(MessageCallBack.ON_DISCONNECT, disconnectProcessor);

        return callback;
    }
}
