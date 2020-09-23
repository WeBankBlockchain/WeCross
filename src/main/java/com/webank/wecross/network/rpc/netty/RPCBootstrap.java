package com.webank.wecross.network.rpc.netty;

import com.webank.wecross.account.AccountManager;
import com.webank.wecross.network.p2p.netty.factory.ThreadPoolTaskExecutorFactory;
import com.webank.wecross.network.rpc.URIHandlerDispatcher;
import com.webank.wecross.network.rpc.authentication.AuthFilter;
import com.webank.wecross.network.rpc.netty.handler.HttpServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.timeout.IdleStateHandler;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/** init RPC service */
public class RPCBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(RPCBootstrap.class);

    /** idle state timeout, 5 min */
    private static final int IDLE_TIMEOUT = 60 * 1000 * 5;
    /** ssl handle shake timeout, 10000 ms */
    private static final int HANDLE_SHAKE_TIMEOUT = 10000;

    private ServerBootstrap serverBootstrap = new ServerBootstrap();
    private RPCConfig config;
    private URIHandlerDispatcher uriHandlerDispatcher;

    private AccountManager accountManager;
    private AuthFilter authFilter;

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public void setAccountManager(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    public RPCConfig getConfig() {
        return config;
    }

    public void setConfig(RPCConfig config) {
        this.config = config;
    }

    public URIHandlerDispatcher getUriHandlerDispatcher() {
        return uriHandlerDispatcher;
    }

    public void setUriHandlerDispatcher(URIHandlerDispatcher uriHandlerDispatcher) {
        this.uriHandlerDispatcher = uriHandlerDispatcher;
    }

    /**
     * init SslContext for http server
     *
     * @param caCrt
     * @param nodeCrt
     * @param nodeKey
     * @return
     * @throws IOException
     */
    public SslContext initSslContextForServer(
            org.springframework.core.io.Resource caCrt,
            org.springframework.core.io.Resource nodeCrt,
            org.springframework.core.io.Resource nodeKey,
            int sslSwitch)
            throws IOException {

        SslContextBuilder sslContextBuilder =
                SslContextBuilder.forServer(nodeCrt.getInputStream(), nodeKey.getInputStream())
                        .trustManager(caCrt.getInputStream())
                        .sslProvider(SslProvider.JDK);

        if (sslSwitch == RPCConfig.SSLSwitch.SSL_ON_CLIENT_AUTH.getSwh()) {
            logger.info(" clientAuth ");
            sslContextBuilder.clientAuth(ClientAuth.REQUIRE);
        }

        return sslContextBuilder.build();
    }

    /**
     * start listen, connect, heartbeat, reconnect mod
     *
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws IOException
     */
    public void start() throws ExecutionException, InterruptedException, IOException {
        startHttpServer();
    }

    /**
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws IOException
     */
    private void startHttpServer() throws ExecutionException, InterruptedException, IOException {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        /** Configure to use SSL, construct SslContext. */
        SslContext sslCtx =
                config.getSslSwitch() == RPCConfig.SSLSwitch.SSL_OFF.getSwh()
                        ? null
                        : initSslContextForServer(
                                config.getCaCert(),
                                config.getSslCert(),
                                config.getSslKey(),
                                config.getSslSwitch());

        ThreadPoolTaskExecutor threadPoolTaskExecutor =
                ThreadPoolTaskExecutorFactory.build(
                        config.getThreadNum(), config.getThreadQueueCapacity(), "http-callback");

        serverBootstrap
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(NioChannelOption.SO_REUSEADDR, true)
                .childHandler(
                        new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) {

                                ChannelPipeline pipeline = ch.pipeline();
                                if (sslCtx != null) {
                                    SslHandler sslHandler = sslCtx.newHandler(ch.alloc());
                                    sslHandler.setHandshakeTimeout(
                                            HANDLE_SHAKE_TIMEOUT, TimeUnit.MILLISECONDS);
                                    pipeline.addLast(sslHandler);
                                }

                                ch.pipeline()
                                        .addLast(
                                                new IdleStateHandler(
                                                        IDLE_TIMEOUT,
                                                        IDLE_TIMEOUT,
                                                        IDLE_TIMEOUT,
                                                        TimeUnit.MILLISECONDS),
                                                new HttpServerCodec(),
                                                new HttpObjectAggregator(Integer.MAX_VALUE),
                                                new HttpServerHandler(
                                                        getUriHandlerDispatcher(),
                                                        threadPoolTaskExecutor,
                                                        accountManager,
                                                        authFilter));
                            }
                        });

        ChannelFuture future = serverBootstrap.bind(config.getListenIP(), config.getListenPort());
        future.get();

        logger.info(
                " start rpc http server, listen ip: {}, port: {}",
                config.getListenIP(),
                config.getListenPort());
    }

    public void setAuthFilter(AuthFilter authFilter) {
        this.authFilter = authFilter;
    }
}
