package com.webank.wecross.p2p.netty.response;

import com.webank.wecross.p2p.netty.SeqMapper;
import com.webank.wecross.p2p.netty.message.proto.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ResponseCallBack {

    private static Logger logger = LoggerFactory.getLogger(ResponseCallBack.class);

    private Message message;
    private Timeout timeout;
    private SeqMapper seqMapper;
    private ChannelHandlerContext ctx;

    public SeqMapper getSeqMapper() {
        return seqMapper;
    }

    public void setSeqMapper(SeqMapper seqMapper) {
        this.seqMapper = seqMapper;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public void cancelTimer() {
        if (getTimeout() != null) {
            getTimeout().cancel();
        }
    }

    /** @param response */
    public abstract void onResponse(Response response);

    public abstract boolean needOnResponse();

    public void sendFailed(int errorCode, String errorMsg) {

        logger.warn(
                " send message not successfully, errorCode: {}, errorMessage: {}, message: {}",
                errorCode,
                errorMsg,
                getMessage());
        try {
            Response response = Response.build(errorCode, errorMsg, message.getSeq(), null);
            onResponse(response);
        } catch (Exception e) {
            logger.warn(" response timeout, seq: {}, e: {}", message.getSeq(), e);
        }

        cancelTimer();
        getSeqMapper().remove(message.getSeq());
    }

    public Timeout getTimeout() {
        return timeout;
    }

    public void setTimeout(Timeout timeout) {
        this.timeout = timeout;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
