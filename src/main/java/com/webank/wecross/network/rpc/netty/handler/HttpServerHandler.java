package com.webank.wecross.network.rpc.netty.handler;

import com.webank.wecross.network.rpc.URIHandlerDispatcher;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.ssl.SslCloseCompletionEvent;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** */
public class HttpServerHandler extends SimpleChannelInboundHandler<HttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);

    private URIHandlerDispatcher uriHandlerDispatcher;

    public HttpServerHandler(URIHandlerDispatcher uriHandlerDispatcher) {
        this.setUriHandlerDispatcher(uriHandlerDispatcher);
    }

    public URIHandlerDispatcher getUriHandlerDispatcher() {
        return uriHandlerDispatcher;
    }

    public void setUriHandlerDispatcher(URIHandlerDispatcher uriHandlerDispatcher) {
        this.uriHandlerDispatcher = uriHandlerDispatcher;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpRequest httpRequest)
            throws Exception {
        final FullHttpRequest fullHttpRequest = (FullHttpRequest) httpRequest;

        if (logger.isDebugEnabled()) {
            logger.debug(
                    " uri: {}, method: {}, version: {}, keepAlive: {}, content: {}",
                    fullHttpRequest.uri(),
                    fullHttpRequest.method(),
                    fullHttpRequest.protocolVersion(),
                    HttpUtil.isKeepAlive(httpRequest),
                    fullHttpRequest.content());
        }

        /** close connection */
        ctx.writeAndFlush("").addListener(ChannelFutureListener.CLOSE);

        /*
        final FullHttpRequest fullHttpRequest = (FullHttpRequest) httpRequest;

        HttpMethod method = fullHttpRequest.method();
        String uri = fullHttpRequest.uri();

        System.out.println("method " + method);
        System.out.println("uri " + uri);
        System.out.println("http version " + fullHttpRequest.protocolVersion());
        System.out.println("keepAlive " + HttpUtil.isKeepAlive(httpRequest));

        String content = fullHttpRequest.content().toString(StandardCharsets.UTF_8);

        System.out.println("content " + content);

        boolean keepAlive = HttpUtil.isKeepAlive(httpRequest);

        FullHttpResponse response =
                new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK,
                        Unpooled.wrappedBuffer(CONTENT.getBytes()));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        // response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            ctx.writeAndFlush(response);
        } else {
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }*/
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        String host = ((SocketChannel) ctx.channel()).remoteAddress().getAddress().getHostAddress();
        Integer port = ((SocketChannel) ctx.channel()).remoteAddress().getPort();
        int hashCode = System.identityHashCode(ctx);

        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {
                case READER_IDLE:
                case WRITER_IDLE:
                case ALL_IDLE:
                    logger.error(
                            " long time inactive, idle state event:{} connect{}:{}",
                            e.state(),
                            host,
                            port);
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
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            " handshake success, host: {}, port: {}, ctx: {}",
                            host,
                            port,
                            hashCode);
                }
            } else {
                logger.error(
                        " handshake failed, host: {}, port: {}, ctx: {}, cause: {} ",
                        host,
                        port,
                        hashCode,
                        e.cause());

                ctx.disconnect();
                ctx.close();
            }
        } else if (evt instanceof SslCloseCompletionEvent) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                        " ssl close completion event, host: {}, port: {}, ctx: {} ",
                        host,
                        port,
                        hashCode);
            }
        } else {
            logger.info(
                    " userEventTriggered event, host: {}, port: {}, evt: {}, ctx: {} ",
                    host,
                    port,
                    evt,
                    hashCode);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        logger.warn(" exceptionCaught, ctx: {}, e: {} ", System.identityHashCode(ctx), cause);

        ctx.disconnect();
        ctx.close();
    }
}
