package com.webank.wecross.p2p.netty.message;

import com.webank.wecross.p2p.netty.SeqMapper;
import com.webank.wecross.p2p.netty.common.Host;
import com.webank.wecross.p2p.netty.common.Utils;
import com.webank.wecross.p2p.netty.message.processor.Processor;
import com.webank.wecross.p2p.netty.message.proto.Message;
import com.webank.wecross.p2p.netty.message.serialize.MessageSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MessageCallBack {

    private static final Logger logger = LoggerFactory.getLogger(MessageCallBack.class);

    /** MessageType => Processor */
    private Map<Short, Processor> messageToProcessor = new ConcurrentHashMap<Short, Processor>() {};

    private SeqMapper seqMapper;

    public SeqMapper getSeqMapper() {
        return seqMapper;
    }

    public void setSeqMapper(SeqMapper seqMapper) {
        this.seqMapper = seqMapper;
    }

    public Map<Short, Processor> getMessageToProcessor() {
        return messageToProcessor;
    }

    public Processor getProcessor(Short type) {
        return messageToProcessor.get(type);
    }

    public void setProcessor(short type, Processor processor) {
        this.messageToProcessor.put(type, processor);
    }

    public void onMessage(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        Host host = Utils.channelContextPeerHost(ctx);
        Integer hashCode = System.identityHashCode(ctx);

        try {
            /** The message header fields are first parsed to get message type, result */
            MessageSerializer messageSerializer = new MessageSerializer();
            Message message = messageSerializer.deserialize(byteBuf);

            logger.trace(
                    " receive message seq: {}, type: {}, host: {}, ctx: {}",
                    message.getSeq(),
                    message.getType(),
                    host,
                    hashCode);

            Processor processor = getProcessor(message.getType());
            if (processor != null) {
                processor.process(ctx, message);
            } else {
                logger.error(
                        " unrecognized message, type: {}, seq: {}, host: {}, ctx: {} ",
                        message.getType(),
                        message.getSeq(),
                        host,
                        hashCode);
            }

        } catch (Exception e) {
            logger.error(" invalid message, host: {}, ctx: {}, e: {}", host, hashCode, e);

        } finally {
            byteBuf.release();
        }
    }
}
