package com.webank.wecross.p2p.netty;

import com.webank.wecross.p2p.netty.channel.handler.ChannelHandlerCallBack;
import com.webank.wecross.p2p.netty.common.Host;
import com.webank.wecross.p2p.netty.common.Peer;
import com.webank.wecross.p2p.netty.common.Utils;
import com.webank.wecross.p2p.netty.message.proto.Message;
import com.webank.wecross.p2p.netty.message.serialize.MessageSerializer;
import com.webank.wecross.p2p.netty.request.Request;
import com.webank.wecross.p2p.netty.response.Response;
import com.webank.wecross.p2p.netty.response.ResponseCallBack;
import com.webank.wecross.p2p.netty.response.StatusCode;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/** P2P service */
public class P2PService {

    private static Logger logger = LoggerFactory.getLogger(P2PService.class);

    private Timer timer = new HashedWheelTimer();

    private Initializer initializer;

    private Connections connections;

    private SeqMapper seqMapper;

    private ThreadPoolTaskExecutor threadPool;

    private ChannelHandlerCallBack channelHandlerCallBack;

    public Initializer getInitializer() {
        return initializer;
    }

    public void setInitializer(Initializer initializer) {
        this.initializer = initializer;
    }

    public SeqMapper getSeqMapper() {
        return seqMapper;
    }

    public void setSeqMapper(SeqMapper seqMapper) {
        this.seqMapper = seqMapper;
    }

    public Connections getConnections() {
        return connections;
    }

    public void setConnections(Connections connections) {
        this.connections = connections;
    }

    public ThreadPoolTaskExecutor getThreadPool() {
        return threadPool;
    }

    public void setThreadPool(ThreadPoolTaskExecutor threadPool) {
        this.threadPool = threadPool;
    }

    public ChannelHandlerCallBack getChannelHandlerCallBack() {
        return channelHandlerCallBack;
    }

    public void setChannelHandlerCallBack(ChannelHandlerCallBack channelHandlerCallBack) {
        this.channelHandlerCallBack = channelHandlerCallBack;
    }

    /**
     * start p2p service
     *
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void start() throws IOException, ExecutionException, InterruptedException {
        initializer.start();
        logger.info(" start p2p service end.");
    }

    /**
     * @param peer
     * @param request
     * @return
     */
    public Response sendRequest(Peer peer, Request request) {

        class Callback extends ResponseCallBack {
            public transient Response response;
            public transient Semaphore semaphore = new Semaphore(1, true);

            Callback() {
                try {
                    semaphore.acquire(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            @Override
            public void onResponse(Response response) {
                this.response = response;
                logger.debug(" callback: request response {}", response);
                semaphore.release();
            }
        }

        Callback callback = new Callback();
        asyncSendRequest(peer, request, callback);

        try {
            callback.semaphore.acquire(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return callback.response;
    }

    /**
     * @param peer
     * @param request
     * @param callback
     */
    public void asyncSendRequest(Peer peer, Request request, ResponseCallBack callback) {

        Message message = Message.builder(request.getType(), request.getContent());

        callback.setMessage(message);
        callback.setSeqMapper(getSeqMapper());

        String nodeID = peer.getNodeID();

        logger.debug(
                " request content, node: {}, seq: {}, type: {}, timeout: {}, content: {}",
                nodeID,
                message.getSeq(),
                message.getType(),
                request.getTimeout(),
                request.getContent());

        // select random nodes to send
        ChannelHandlerContext ctx = getConnections().getChannelHandler(nodeID);
        if (ctx != null && ctx.channel().isActive()) {
            callback.setCtx(ctx);
            getSeqMapper().add(message.getSeq(), callback);

            if (request.getTimeout() > 0) {

                final ResponseCallBack finalCallback = callback;
                final ThreadPoolTaskExecutor finalThreadPool = threadPool;
                callback.setTimeout(
                        timer.newTimeout(
                                new TimerTask() {
                                    @Override
                                    public void run(Timeout timeout) throws Exception {
                                        if (finalThreadPool == null) {
                                            callback.sendFailed(StatusCode.TIMEOUT, "timeout");
                                        } else {
                                            finalThreadPool.execute(
                                                    new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            callback.sendFailed(
                                                                    StatusCode.TIMEOUT, "timeout");
                                                        }
                                                    });
                                        }
                                    }
                                },
                                request.getTimeout(),
                                TimeUnit.MILLISECONDS));
            }

            MessageSerializer serializer = new MessageSerializer();
            ByteBuf byteBuf = ctx.alloc().buffer();
            serializer.serialize(message, byteBuf);
            ctx.writeAndFlush(byteBuf);

            Host host = Utils.channelContextPeerHost(ctx);

            logger.debug(" send request, host: {}, seq: {}", host, message.getSeq());
        } else {
            callback.sendFailed(StatusCode.UNREACHABLE, "node unreachable");
        }
    }

    public Set<Peer> getConnectedPeers() {
        return getConnections().getPeers();
    }
}
