package com.webank.wecross.config;

import com.webank.wecross.p2p.netty.NettyBootstrap;
import com.webank.wecross.p2p.netty.channel.handler.ChannelHandlerCallBack;
import com.webank.wecross.p2p.netty.message.MessageCallBack;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class NettyBootstrapConfig {
    @Resource P2PConfig p2pConfig;

    @Resource MessageCallBack messageCallBack;

    @Resource ThreadPoolTaskExecutor threadPool;

    @Bean
    public NettyBootstrap newNettyBootstrap() {
        System.out.println("Initializing NettyBootstrap ...");

        ChannelHandlerCallBack channelHandlerCallBack = new ChannelHandlerCallBack();
        channelHandlerCallBack.setThreadPool(threadPool);

        NettyBootstrap bootstrap = new NettyBootstrap();
        bootstrap.setConfig(p2pConfig);
        bootstrap.setMessageCallBack(messageCallBack);
        bootstrap.setChannelHandlerCallBack(channelHandlerCallBack);

        return bootstrap;
    }
}
