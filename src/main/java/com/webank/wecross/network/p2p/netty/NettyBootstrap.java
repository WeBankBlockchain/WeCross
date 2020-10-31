package com.webank.wecross.network.p2p.netty;

import com.webank.wecross.network.p2p.netty.channel.handler.ChannelHandler;
import com.webank.wecross.network.p2p.netty.channel.handler.ChannelHandlerCallBack;
import com.webank.wecross.network.p2p.netty.common.Node;
import com.webank.wecross.network.p2p.netty.factory.P2PConfig;
import com.webank.wecross.network.p2p.netty.message.proto.Message;
import com.webank.wecross.network.p2p.netty.message.serialize.MessageSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

/** init P2P service */
public class NettyBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(NettyBootstrap.class);

    /** Maximum message length of p2p message, default 64MB */
    private static final Integer maxP2PMessageLength = 64 * 1024 * 1024;
    /** P2P connection maximum idle time, default 20000 ms */
    private static final Integer connectionIdleTimeoutMS = 20000;
    /** P2P connection hearbeat period, default 3000ms */
    private static final Integer heartBeatPeriod = 3000;
    /** P2P reconnect period, default 30000 ms */
    private static final Integer reconnectPeriod = 30000;
    /** ssl handle shake timeout default 10000 ms */
    private static final Integer handShakeTimeoutMS = 10000;

    private ChannelHandlerCallBack channelHandlerCallBack;
    private Connections connections = new Connections();
    private P2PConfig config;

    public P2PConfig getConfig() {
        return config;
    }

    public void setConfig(P2PConfig config) {
        this.config = config;
    }

    public ChannelHandlerCallBack getChannelHandlerCallBack() {
        return channelHandlerCallBack;
    }

    public void setChannelHandlerCallBack(ChannelHandlerCallBack channelHandlerCallBack) {
        this.channelHandlerCallBack = channelHandlerCallBack;
    }

    public Connections getConnections() {
        return connections;
    }

    public void setConnections(Connections connections) {
        this.connections = connections;
    }

    private final Bootstrap bootstrap = new Bootstrap();
    private final ServerBootstrap serverBootstrap = new ServerBootstrap();

    private ScheduledExecutorService scheduledExecutorService =
            new ScheduledThreadPoolExecutor(1, new CustomizableThreadFactory("p2pbootstrap-"));

    /**
     * init SslContext for p2p connection
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
            org.springframework.core.io.Resource nodeKey)
            throws IOException {

        SslContext sslCtx =
                SslContextBuilder.forServer(nodeCrt.getInputStream(), nodeKey.getInputStream())
                        .trustManager(caCrt.getInputStream())
                        .sslProvider(SslProvider.JDK)
                        .clientAuth(ClientAuth.REQUIRE)
                        .build();

        return sslCtx;
    }

    /**
     * init SslContext for p2p connection
     *
     * @param caCrt
     * @param nodeCrt
     * @param nodeKey
     * @return
     * @throws IOException
     */
    public SslContext initSslContextForClient(
            org.springframework.core.io.Resource caCrt,
            org.springframework.core.io.Resource nodeCrt,
            org.springframework.core.io.Resource nodeKey)
            throws IOException {
        return SslContextBuilder.forClient()
                .trustManager(caCrt.getInputStream())
                .keyManager(nodeCrt.getInputStream(), nodeKey.getInputStream())
                .sslProvider(SslProvider.JDK)
                .clientAuth(ClientAuth.REQUIRE)
                .build();
    }

    /**
     * start listen, connect, heartbeat, reconnect mod
     *
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws IOException
     */
    public void start() throws ExecutionException, InterruptedException, IOException {
        channelHandlerCallBack.setConnections(connections);

        logger.info(" initialize, config: {}", getConfig());

        // check parameter first
        getConfig().validConfig();
        // set connect peer nodes
        getConnections().setConfiguredPeers(getConfig().getConnectPeers());

        startListen();
        startConnect();
        startPeriodTasks();
    }

    private void startListen() throws ExecutionException, InterruptedException, IOException {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        P2PConfig config = getConfig();

        SslContext sslCtx =
                initSslContextForServer(
                        config.getCaCert(), config.getSslCert(), config.getSslKey());

        serverBootstrap
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 100)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(
                        new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) {
                                /*
                                 * Each connection is fetched from the socketChannel, using the new handler connection information
                                 */
                                ChannelHandler handler = new ChannelHandler();
                                handler.setChannelHandlerCallBack(getChannelHandlerCallBack());

                                SslHandler sslHandler = sslCtx.newHandler(ch.alloc());
                                sslHandler.setHandshakeTimeout(
                                        handShakeTimeoutMS, TimeUnit.MILLISECONDS);

                                ch.pipeline()
                                        .addLast(
                                                sslHandler,
                                                new LengthFieldBasedFrameDecoder(
                                                        Integer.MAX_VALUE, 0, 4, -4, 0),
                                                new IdleStateHandler(
                                                        connectionIdleTimeoutMS,
                                                        connectionIdleTimeoutMS,
                                                        connectionIdleTimeoutMS,
                                                        TimeUnit.MILLISECONDS),
                                                handler);
                            }
                        });

        ChannelFuture future = serverBootstrap.bind(config.getListenIP(), config.getListenPort());
        future.get();

        logger.info(
                " start listen, ip: {}, port: {}", config.getListenIP(), config.getListenPort());
    }

    private void startConnect() throws IOException {

        // init netty
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        bootstrap.group(workerGroup);
        bootstrap.channel(NioSocketChannel.class);

        P2PConfig config = getConfig();

        SslContext sslCtx =
                initSslContextForClient(
                        config.getCaCert(), config.getSslCert(), config.getSslKey());

        bootstrap.handler(
                new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        /*
                         * Each connection is fetched from the socketChannel, using the new handler connection information
                         */
                        ChannelHandler handler = new ChannelHandler();
                        handler.setConnectToServer(true);
                        handler.setChannelHandlerCallBack(getChannelHandlerCallBack());

                        SslHandler sslHandler = sslCtx.newHandler(ch.alloc());
                        sslHandler.setHandshakeTimeout(handShakeTimeoutMS, TimeUnit.MILLISECONDS);

                        ch.pipeline()
                                .addLast(
                                        sslHandler,
                                        new LengthFieldBasedFrameDecoder(
                                                maxP2PMessageLength, 0, 4, -4, 0),
                                        new IdleStateHandler(
                                                connectionIdleTimeoutMS,
                                                connectionIdleTimeoutMS,
                                                connectionIdleTimeoutMS,
                                                TimeUnit.MILLISECONDS),
                                        handler);
                    }
                });

        // Connect all configured nodes
        logger.info(" start connect, config: {}", config);
    }

    private void startPeriodTasks() {
        // heartbeat: 3 s
        scheduledExecutorService.scheduleAtFixedRate(
                () -> heartBeat(), 0, heartBeatPeriod, TimeUnit.MILLISECONDS);

        // reconnect : 30s
        scheduledExecutorService.scheduleAtFixedRate(
                () -> {
                    reconnect();
                    listConnectedNodes();
                },
                0,
                reconnectPeriod,
                TimeUnit.MILLISECONDS);
    }

    /** list all connected nodes */
    public void listConnectedNodes() {
        Map<String, String> host2NodeID = getConnections().getHost2NodeID();
        synchronized (host2NodeID) {
            host2NodeID.forEach(
                    (host, nodeID) -> {
                        logger.info("Current peer url:{} nodeid:{}", host, nodeID);
                    });
        }
    }

    /** send heartbeat message to all active nodes */
    public void heartBeat() {
        List<ChannelHandlerContext> channelHandlers = getConnections().activeChannelHandlers();
        channelHandlers.forEach(
                (ctx) -> {
                    Message message = Message.builder(MessageType.HEARTBEAT);
                    MessageSerializer serializer = new MessageSerializer();
                    ByteBuf byteBuf = ctx.alloc().buffer();
                    serializer.serialize(message, byteBuf);
                    ctx.writeAndFlush(byteBuf);

                    Node node = (Node) ctx.channel().attr(AttributeKey.valueOf("node")).get();

                    logger.trace(" send heartbeat message to {} ", node);
                });
    }

    /** reconnect all configured nodes */
    public synchronized void reconnect() {

        getConnections()
                .shouldConnectNodes()
                .forEach(
                        host -> {
                            ChannelFuture channelFuture =
                                    bootstrap.connect(host.getHost(), host.getPort());
                            channelFuture.addListener(
                                    new ChannelFutureListener() {
                                        @Override
                                        public void operationComplete(ChannelFuture future) {
                                            if (future.isSuccess()) {
                                                logger.debug(" connect to {} success", host);
                                            } else {
                                                logger.warn(
                                                        " connect to {} failed, error: {}",
                                                        host,
                                                        future.cause());
                                            }
                                        }
                                    });
                            logger.debug(" try to connect {}", host);
                        });
    }

    public synchronized void removeConnect(String ipPort) {

        String nodeID = getConnections().getHost2NodeID().get(ipPort);
        if (nodeID == null) {
            return;
        }

        ChannelHandlerContext ctx = getConnections().getChannelHandler(nodeID);
        if (ctx != null) {
            logger.debug("Try to disconnect {}", nodeID);
            getChannelHandlerCallBack().onDisconnect(ctx);
            ctx.disconnect();
            ctx.close();
            logger.debug("Disconnected: {}", nodeID);
        }
    }

    public void addConfiguredPeer(String ipPort) throws InvalidParameterException {
        addConfiguredPeer(P2PConfig.toHost(ipPort));
    }

    public void addConfiguredPeer(Node node) {
        getConnections().addConfiguredPeer(node);
        reconnect();
        logger.info(
                "Add configured peer {}, configuredPeers: {}",
                node,
                getConnections().getConfiguredPeers());
    }

    public void removeConfiguredPeer(String ipPort) {
        getConnections().removeConfiguredPeer(ipPort);
        removeConnect(ipPort);
        logger.info(
                "Remove configured peer {}, configuredPeers: {}",
                ipPort,
                getConnections().getConfiguredPeers());
    }
}
