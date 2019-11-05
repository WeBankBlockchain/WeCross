package com.webank.wecross.p2p.netty.channel.handler;

import com.webank.wecross.p2p.netty.common.Host;
import com.webank.wecross.p2p.netty.common.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private static Logger logger = LoggerFactory.getLogger(ChannelHandler.class);

    /** Actively connect to other clients or other clients connect to me */
    private Boolean connectToServer = false;

    /** channel handler callback */
    private ChannelHandlerCallBack channelHandlerCallBack;

    public Boolean getConnectToServer() {
        return connectToServer;
    }

    public void setConnectToServer(Boolean connectToServer) {
        this.connectToServer = connectToServer;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        Host host = Utils.channelContextPeerHost(ctx);
        int hashCode = System.identityHashCode(ctx);

        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {
                case READER_IDLE:
                case WRITER_IDLE:
                case ALL_IDLE:
                    logger.error(
                            " disconnect, event:{} host:{} ctx:{}, long time inactive",
                            e.state(),
                            host,
                            hashCode);
                    channelInactive(ctx);
                    ctx.disconnect();
                    ctx.close();
                    break;
                default:
                    break;
            }
        } else if (evt instanceof SslHandshakeCompletionEvent) {
            logger.info(" handshake success, host: {}, ctx: {}", host, hashCode);
            // ssl handshake success.
            try {
                getChannelHandlerCallBack().onConnect(ctx, getConnectToServer());
            } catch (Exception e) {
                logger.error(
                        " disconnect, host: {}, ctx: {}, error: {}",
                        host,
                        hashCode,
                        e.getMessage());
                ctx.disconnect();
                ctx.close();
            }
        } else {
            logger.info(" host: {}, ctx: {}, evt: {}", host, hashCode, evt);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Host host = Utils.channelContextPeerHost(ctx);
        logger.info(" channel active: {}, ctx: {}", host, System.identityHashCode(ctx));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Host host = Utils.channelContextPeerHost(ctx);
        try {
            logger.info(" channel inactive: {}", host);

            getChannelHandlerCallBack().onDisconnect(ctx);
        } catch (Exception e) {
            logger.error("host: {}, ctx: {}, error: {}", host, System.identityHashCode(ctx), e);
        }
    }

    public ChannelHandlerCallBack getChannelHandlerCallBack() {
        return channelHandlerCallBack;
    }

    public void setChannelHandlerCallBack(ChannelHandlerCallBack channelHandlerCallBack) {
        this.channelHandlerCallBack = channelHandlerCallBack;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        Host host = Utils.channelContextPeerHost(ctx);
        logger.error(" caugth exception, e: {}, host: {}", cause, host);

        ctx.disconnect();
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        getChannelHandlerCallBack().onMessage(ctx, (ByteBuf) msg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf in) {
        channelRead(ctx, in);
    }
}
