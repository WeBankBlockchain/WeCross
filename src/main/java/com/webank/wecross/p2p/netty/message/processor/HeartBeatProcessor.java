package com.webank.wecross.p2p.netty.message.processor;

import com.webank.wecross.p2p.netty.common.Host;
import com.webank.wecross.p2p.netty.common.Utils;
import com.webank.wecross.p2p.netty.message.proto.Message;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HeartBeatProcessor implements Processor {

    private static final Logger logger = LoggerFactory.getLogger(HeartBeatProcessor.class);

    @Override
    public String name() {
        return "HeartBeat";
    }

    @Override
    public void process(ChannelHandlerContext ctx, Message message) {
        Host host = Utils.channelContextPeerHost(ctx);
        // log with do nothing
        logger.trace(" receive heartbeat, host: {}, seq: {}", host, message.getSeq());
    }
}
