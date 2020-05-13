package com.webank.wecross.network.p2p.netty.message.processor;

import com.webank.wecross.network.p2p.netty.common.Node;
import com.webank.wecross.network.p2p.netty.message.proto.Message;
import io.netty.channel.ChannelHandlerContext;

/** Process the received P2p message interface */
public interface Processor {
    /**
     * the name of this processor
     *
     * @return
     */
    String name();

    /**
     * Process the received P2P message interface, you should in complete the follow functions: 1.
     * parse the `message` into corresponding java object 2. complete the logic for the message
     *
     * @param ctx
     * @param message
     */
    void process(ChannelHandlerContext ctx, Node node, Message message);
}
