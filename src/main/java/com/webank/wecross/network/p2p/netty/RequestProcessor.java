package com.webank.wecross.network.p2p.netty;

import com.webank.wecross.network.NetworkProcessor;
import com.webank.wecross.network.p2p.netty.common.Node;
import com.webank.wecross.network.p2p.netty.message.processor.Processor;
import com.webank.wecross.network.p2p.netty.message.proto.Message;
import com.webank.wecross.network.p2p.netty.message.serialize.MessageSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestProcessor implements Processor {
    private static final Logger logger = LoggerFactory.getLogger(RequestProcessor.class);

    private NetworkProcessor networkProcessor;

    @Override
    public String name() {
        return "RequestProcessor";
    }

    @Override
    public void process(ChannelHandlerContext ctx, Node node, Message message) {
        try {
            String content = new String(message.getData(), "utf-8");

            logger.debug(
                    "  resource request message, host: {}, seq: {}, content: {}",
                    node,
                    message.getSeq(),
                    content);

            networkProcessor.process(
                    node,
                    content,
                    new NetworkProcessor.Callback() {
                        @Override
                        public void onResponse(String responseContent) {
                            if (responseContent != null) {

                                // send response
                                message.setType(MessageType.RESOURCE_RESPONSE);
                                message.setData(responseContent.getBytes());

                                MessageSerializer serializer = new MessageSerializer();
                                ByteBuf byteBuf = ctx.alloc().buffer();
                                serializer.serialize(message, byteBuf);
                                ctx.writeAndFlush(byteBuf);

                                logger.debug(
                                        " resource request, host: {}, seq: {}, response content: {}",
                                        node,
                                        message.getSeq(),
                                        responseContent);
                            } else {
                                logger.error(
                                        "response content is null, node: "
                                                + node
                                                + ", content: "
                                                + content);
                            }
                        }
                    });

        } catch (Exception e) {
            logger.error(" invalid format, host: {}, e: {}", node, e);
        }
    }

    public void setNetworkProcessor(NetworkProcessor networkProcessor) {
        this.networkProcessor = networkProcessor;
    }
}
