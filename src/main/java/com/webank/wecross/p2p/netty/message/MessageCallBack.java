package com.webank.wecross.p2p.netty.message;

import com.webank.wecross.p2p.HeartBeatProcessor;
import com.webank.wecross.p2p.MessageType;
import com.webank.wecross.p2p.ResourceRequestProcessor;
import com.webank.wecross.p2p.ResourceResponseProcessor;
import com.webank.wecross.p2p.netty.SeqMapper;
import com.webank.wecross.p2p.netty.common.Node;
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

public class MessageCallBack {

    private static final Logger logger = LoggerFactory.getLogger(MessageCallBack.class);

    /** MessageType => Processor */
    private Map<Short, Processor> messageToProcessor = new ConcurrentHashMap<Short, Processor>() {};
    private Processor connectProcessor;

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

    public HeartBeatProcessor getHeartBeatProcessor() {
        return (HeartBeatProcessor) messageToProcessor.get(MessageType.HEARTBEAT);
    }

    public ResourceResponseProcessor getResourceResponseProcessor() {
        return (ResourceResponseProcessor) messageToProcessor.get(MessageType.RESOURCE_RESPONSE);
    }

    public ResourceRequestProcessor getResourceRequestProcessor() {
        return (ResourceRequestProcessor) messageToProcessor.get(MessageType.RESOURCE_REQUEST);
    }
    
    public void onConnect(ChannelHandlerContext ctx, Node node) {
    	if(connectProcessor != null) {
    		connectProcessor.process(ctx, node, null);
    	}
    }
    
    public void onDisconnect(ChannelHandlerContext ctx, Node node) {
    	if(connectProcessor != null) {
    		connectProcessor.process(ctx, node, null);
    	}
    }

    public void onMessage(ChannelHandlerContext ctx, Node node, ByteBuf byteBuf) {
        Integer hashCode = System.identityHashCode(ctx);

        try {
            /** The message header fields are first parsed to get message type, result */
            MessageSerializer messageSerializer = new MessageSerializer();
            Message message = messageSerializer.deserialize(byteBuf);

            logger.trace(
                    " receive message seq: {}, type: {}, host: {}, ctx: {}",
                    message.getSeq(),
                    message.getType(),
                    node,
                    hashCode);

            Processor processor = getProcessor(message.getType());
            if (processor != null) {
                processor.process(ctx, node, message);
            } else {
                logger.error(
                        " unrecognized message, type: {}, seq: {}, host: {}, ctx: {} ",
                        message.getType(),
                        message.getSeq(),
                        node,
                        hashCode);
            }

        } catch (Exception e) {
            logger.error(" invalid message, host: {}, ctx: {}, e: {}", node, hashCode, e);

        } finally {
            byteBuf.release();
        }
    }
}
