package com.webank.wecross.network.p2p.netty.factory;

import com.webank.wecross.network.p2p.netty.NettyBootstrap;
import com.webank.wecross.network.p2p.netty.channel.handler.ChannelHandlerCallBack;
import com.webank.wecross.network.p2p.netty.message.MessageCallBack;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class NettyBootstrapFactory {
    public static NettyBootstrap build(
            P2PConfig p2pConfig,
            ThreadPoolTaskExecutor threadPool,
            MessageCallBack messageCallBack) {
        System.out.println("Initializing NettyBootstrap ...");

        ChannelHandlerCallBack channelHandlerCallBack = new ChannelHandlerCallBack();
        channelHandlerCallBack.setThreadPool(threadPool);
        channelHandlerCallBack.setCallBack(messageCallBack);

        NettyBootstrap bootstrap = new NettyBootstrap();
        bootstrap.setConfig(p2pConfig);
        bootstrap.setChannelHandlerCallBack(channelHandlerCallBack);

        return bootstrap;
    }
}
