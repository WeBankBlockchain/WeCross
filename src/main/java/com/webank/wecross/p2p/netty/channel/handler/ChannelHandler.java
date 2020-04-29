package com.webank.wecross.p2p.netty.channel.handler;

import com.webank.wecross.p2p.netty.common.Node;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
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
        Node node = (Node) (ctx.channel().attr(AttributeKey.valueOf("node")).get());
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
                            node,
                            hashCode);
                    channelInactive(ctx);
                    ctx.disconnect();
                    ctx.close();
                    break;
                default:
                    break;
            }
        } else if (evt instanceof SslHandshakeCompletionEvent) {
            SslHandshakeCompletionEvent e = (SslHandshakeCompletionEvent) evt;
            if (e.isSuccess()) {
                logger.info(" handshake success, host: {}, ctx: {}", node, hashCode);
                try {
                    getChannelHandlerCallBack().onConnect(ctx, getConnectToServer());
                } catch (Exception e1) {
                    logger.warn(
                            " handshake on connect exception, disconnect, host: {}, ctx: {}, cause: {}",
                            node,
                            hashCode,
                            e1.getCause());
                    ctx.disconnect();
                    ctx.close();
                }
            } else {
                logger.warn(
                        " handshake failed, host: {}, message: {}, cause: {} ",
                        node,
                        e.cause().getMessage(),
                        e.cause());

                ctx.disconnect();
                ctx.close();
            }
        } else {
            logger.info(" host: {}, ctx: {}, evt: {}", node, hashCode, evt);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        int hashCode = System.identityHashCode(ctx);
        logger.trace(
                " channelActive, node: {}:{}, ctx: {}",
                ((SocketChannel) ctx.channel()).remoteAddress().getAddress().getHostAddress(),
                ((SocketChannel) ctx.channel()).remoteAddress().getPort(),
                hashCode);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        int hashCode = System.identityHashCode(ctx);
        try {
            logger.info(" channel inactive, ctx: {}", hashCode);

            getChannelHandlerCallBack().onDisconnect(ctx);
        } catch (Exception e) {
            logger.error(" channelInactive exception, ctx: {}, error: {}", hashCode, e);
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
        logger.error(
                " caught exception, e: {}, node: {}:{}",
                cause,
                ((SocketChannel) ctx.channel()).remoteAddress().getAddress().getHostAddress(),
                ((SocketChannel) ctx.channel()).remoteAddress().getPort());

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
