package com.webank.wecross.p2p;

import com.webank.wecross.p2p.netty.SeqMapper;
import com.webank.wecross.p2p.netty.common.Node;
import com.webank.wecross.p2p.netty.message.processor.Processor;
import com.webank.wecross.p2p.netty.message.proto.Message;
import com.webank.wecross.p2p.netty.response.Response;
import com.webank.wecross.p2p.netty.response.ResponseCallBack;
import com.webank.wecross.p2p.netty.response.StatusCode;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseProcessor implements Processor {

    private static final Logger logger = LoggerFactory.getLogger(ResponseProcessor.class);

    @Override
    public String name() {
        return "ResourceResponse";
    }

    private SeqMapper seqMapper;

    public SeqMapper getSeqMapper() {
        return seqMapper;
    }

    public void setSeqMapper(SeqMapper seqMapper) {
        this.seqMapper = seqMapper;
    }

    @Override
    public void process(ChannelHandlerContext ctx, Node node, Message message) {
        try {
            String content = new String(message.getData(), "utf-8");
            logger.trace(" source response, message: {}, content: {}", message, content);

            ResponseCallBack callback =
                    (ResponseCallBack) getSeqMapper().getAndRemove(message.getSeq());
            if (null == callback) {
                throw new UnsupportedOperationException(
                        " not found callback, seq: " + message.getSeq());
            }

            // cancel timeout firsts
            callback.cancelTimer();

            Response response =
                    Response.build(StatusCode.SUCCESS, "success", message.getSeq(), content);
            callback.onResponse(response);

        } catch (Exception e) {
            logger.error(" e: {}", e);
        }
    }
}
