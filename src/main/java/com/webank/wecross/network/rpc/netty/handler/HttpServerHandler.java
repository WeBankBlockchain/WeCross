package com.webank.wecross.network.rpc.netty.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.account.AccountManager;
import com.webank.wecross.account.UserContext;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.network.rpc.URIHandlerDispatcher;
import com.webank.wecross.network.rpc.authentication.AuthFilter;
import com.webank.wecross.network.rpc.handler.URIHandler;
import com.webank.wecross.network.rpc.netty.URIMethod;
import com.webank.wecross.restserver.RestResponse;
import com.webank.wecross.stub.ObjectMapperFactory;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.ssl.SslCloseCompletionEvent;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.handler.timeout.IdleStateEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import javax.activation.MimetypesFileTypeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/** */
public class HttpServerHandler extends SimpleChannelInboundHandler<HttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);

    private URIHandlerDispatcher uriHandlerDispatcher;
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    private AuthFilter authFilter;
    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    private MimetypesFileTypeMap mimeTypesMap;
    private AccountManager accountManager;

    public HttpServerHandler(
            URIHandlerDispatcher uriHandlerDispatcher,
            ThreadPoolTaskExecutor threadPoolTaskExecutor,
            MimetypesFileTypeMap mimeTypesMap,
            AuthFilter authFilter,
            AccountManager accountManager) {
        this.mimeTypesMap = mimeTypesMap;
        this.authFilter = authFilter;
        this.accountManager = accountManager;
        this.setUriHandlerDispatcher(uriHandlerDispatcher);
        this.setThreadPoolTaskExecutor(threadPoolTaskExecutor);
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
        String urlPrefix = this.uriHandlerDispatcher.getUrlPrefix();
        if (urlPrefix != null) {
            if (!httpRequest.uri().startsWith(urlPrefix)) {
                logger.error(
                        "URI did not start with prefix, URI:{}, prefix:{}",
                        httpRequest.uri(),
                        urlPrefix);
                throw new WeCrossException(
                        WeCrossException.ErrorCode.PAGE_NOT_FOUND,
                        "URI did not start with prefix while prefix is not null, prefix:"
                                + urlPrefix);
            } else {
                httpRequest.setUri(httpRequest.uri().substring(urlPrefix.length()));
            }
        }
        authFilter.doAuth(
                ctx,
                httpRequest,
                new AuthFilter.Handler() {
                    @Override
                    public void onAuth(ChannelHandlerContext ctx, HttpRequest httpRequest)
                            throws Exception {
                        doRouterRead(ctx, httpRequest);
                    }
                });
    }

    protected void doRouterRead(ChannelHandlerContext ctx, HttpRequest httpRequest)
            throws Exception {
        final FullHttpRequest fullHttpRequest = (FullHttpRequest) httpRequest;

        String uri = fullHttpRequest.uri();
        HttpMethod method = fullHttpRequest.method();
        HttpVersion httpVersion = fullHttpRequest.protocolVersion();
        String content = fullHttpRequest.content().toString(StandardCharsets.UTF_8);
        boolean keepAlive = HttpUtil.isKeepAlive(httpRequest);
        long currentTimeMillis = System.currentTimeMillis();

        if (logger.isDebugEnabled()) {
            logger.debug(
                    " uri: {}, method: {}, version: {}, keepAlive: {}, content: {}",
                    uri,
                    method,
                    httpVersion,
                    keepAlive,
                    fullHttpRequest.content().toString(StandardCharsets.UTF_8));
        }

        URIHandler uriHandler =
                uriHandlerDispatcher.matchURIHandler(new URIMethod(method.name(), uri));

        // Not found handler
        if (Objects.isNull(uriHandler)) {
            logger.debug("Not found, uri: {}, method: {}", uri, method);

            FullHttpResponse response =
                    new DefaultFullHttpResponse(
                            HttpVersion.HTTP_1_1,
                            HttpResponseStatus.NOT_FOUND,
                            Unpooled.wrappedBuffer("".getBytes()));

            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
            response.headers()
                    .set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            // close connection
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            return;
        }

        threadPoolTaskExecutor.execute(
                () -> {
                    UserContext userContext = new UserContext();

                    String token = getTokenFromHeader(httpRequest);
                    userContext.setToken(token);

                    if (shouldLogin(uri)) {
                        if (!hasLogin(userContext)) {
                            String errorMessage =
                                    "Login check failed, uri: "
                                            + uri
                                            + " token:"
                                            + userContext.getToken();
                            logger.warn(errorMessage);
                            unauthorizedResponse(ctx, errorMessage);
                            return; // not auth, just return
                        }
                    }

                    uriHandler.handle(
                            userContext,
                            uri,
                            method.toString(),
                            content,
                            new URIHandler.Callback() {

                                private void write(FullHttpResponse response) {
                                    if (logger.isDebugEnabled()) {
                                        logger.debug(
                                                " ===>>> content: {}",
                                                response.content().toString());
                                    }

                                    // check if connection active
                                    if (!ctx.channel().isActive()) {
                                        long currentTimeMillis1 = System.currentTimeMillis();
                                        logger.warn(
                                                " ctx: {} not active, cost: {} ",
                                                System.identityHashCode(ctx),
                                                (currentTimeMillis1 - currentTimeMillis));
                                        return;
                                    }

                                    response.headers()
                                            .set(
                                                    HttpHeaderNames.CONTENT_LENGTH,
                                                    response.content().readableBytes());

                                    if (keepAlive) {
                                        response.headers()
                                                .set(
                                                        HttpHeaderNames.CONNECTION,
                                                        HttpHeaderValues.KEEP_ALIVE);
                                        ctx.writeAndFlush(response);
                                    } else {
                                        ctx.writeAndFlush(response)
                                                .addListener(ChannelFutureListener.CLOSE);
                                    }
                                }

                                @Override
                                public void onResponse(RestResponse restResponse) {
                                    String restResponseStr = "";
                                    try {
                                        restResponseStr =
                                                objectMapper.writeValueAsString(restResponse);
                                    } catch (JsonProcessingException e) {
                                        logger.warn("writeValueAsString e: {}", e);
                                        return;
                                    }

                                    FullHttpResponse response =
                                            new DefaultFullHttpResponse(
                                                    HttpVersion.HTTP_1_1,
                                                    HttpResponseStatus.OK,
                                                    Unpooled.wrappedBuffer(
                                                            restResponseStr.getBytes()));

                                    response.headers()
                                            .set(HttpHeaderNames.CONTENT_TYPE, "application/json");
                                    write(response);
                                }

                                @Override
                                public void onResponse(String restResponse) {
                                    FullHttpResponse response =
                                            new DefaultFullHttpResponse(
                                                    HttpVersion.HTTP_1_1,
                                                    HttpResponseStatus.OK,
                                                    Unpooled.wrappedBuffer(
                                                            restResponse.getBytes()));

                                    response.headers()
                                            .set(HttpHeaderNames.CONTENT_TYPE, "text/html");

                                    write(response);
                                }

                                @Override
                                public void onResponse(FullHttpResponse fullHttpResponse) {
                                    write(fullHttpResponse);
                                }

                                @Override
                                public void onResponse(File restResponse) {
                                    byte[] content;

                                    try {
                                        content = Files.readAllBytes(restResponse.toPath());
                                    } catch (Exception e) {
                                        logger.warn("readAllBytes e: {}", e);
                                        return;
                                    }

                                    FullHttpResponse response =
                                            new DefaultFullHttpResponse(
                                                    HttpVersion.HTTP_1_1,
                                                    HttpResponseStatus.OK,
                                                    Unpooled.wrappedBuffer(content));

                                    response.headers()
                                            .set(
                                                    HttpHeaderNames.CONTENT_TYPE,
                                                    mimeTypesMap.getContentType(restResponse));

                                    write(response);
                                }
                            });
                });
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        if (logger.isDebugEnabled()) {
            String host =
                    ((SocketChannel) ctx.channel()).remoteAddress().getAddress().getHostAddress();
            Integer port = ((SocketChannel) ctx.channel()).remoteAddress().getPort();
            int hashCode = System.identityHashCode(ctx);
            logger.debug(" channelActive, {}:{}, ctx: {}", host, port, hashCode);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        if (logger.isDebugEnabled()) {
            String host =
                    ((SocketChannel) ctx.channel()).remoteAddress().getAddress().getHostAddress();
            Integer port = ((SocketChannel) ctx.channel()).remoteAddress().getPort();
            int hashCode = System.identityHashCode(ctx);
            logger.debug(" channelInactive, {}:{}, ctx: {}", host, port, hashCode);
        }
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

    public ThreadPoolTaskExecutor getThreadPoolTaskExecutor() {
        return threadPoolTaskExecutor;
    }

    public void setThreadPoolTaskExecutor(ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
    }

    private String getTokenFromHeader(HttpRequest httpRequest) {
        String tokenStr = httpRequest.headers().get(HttpHeaders.Names.AUTHORIZATION);
        if (tokenStr == null || tokenStr.length() == 0) {
            return null;
        } else {
            return tokenStr;
        }
    }

    public static boolean shouldLogin(String uri) {

        String[] rawUriSplits = uri.split("\\?");
        String[] splits = rawUriSplits[0].split("/");
        String uriMethod = splits[splits.length - 1];

        if (uriMethod.startsWith("test")
                || uriMethod.startsWith("status")
                || uriMethod.startsWith("supportedStubs")
                || uriMethod.startsWith("login")
                || uriMethod.startsWith("register")
                || splits[1].equalsIgnoreCase("s")) {
            return false;
        }

        return true;
    }

    private boolean hasLogin(UserContext userContext) {
        try {
            accountManager.checkLogin(userContext);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void unauthorizedResponse(ChannelHandlerContext ctx, String message) {
        FullHttpResponse response =
                new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.UNAUTHORIZED,
                        Unpooled.wrappedBuffer(message.getBytes()));

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        // close connection
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
