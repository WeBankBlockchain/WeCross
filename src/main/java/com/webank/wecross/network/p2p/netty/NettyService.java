package com.webank.wecross.network.p2p.netty;

import com.webank.wecross.network.p2p.netty.channel.handler.ChannelHandlerCallBack;
import com.webank.wecross.network.p2p.netty.common.Node;
import com.webank.wecross.network.p2p.netty.message.proto.Message;
import com.webank.wecross.network.p2p.netty.message.serialize.MessageSerializer;
import com.webank.wecross.network.p2p.netty.request.Request;
import com.webank.wecross.network.p2p.netty.response.Response;
import com.webank.wecross.network.p2p.netty.response.ResponseCallBack;
import com.webank.wecross.network.p2p.netty.response.StatusCode;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/** P2P service */
public class NettyService {
    private static Logger logger = LoggerFactory.getLogger(NettyService.class);

    private Timer timer = new HashedWheelTimer();
    private NettyBootstrap nettyBootstrap;
    private SeqMapper seqMapper;
    private ThreadPoolTaskExecutor threadPool;

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public NettyBootstrap getInitializer() {
        return nettyBootstrap;
    }

    public void setInitializer(NettyBootstrap initializer) {
        this.nettyBootstrap = initializer;
    }

    public SeqMapper getSeqMapper() {
        return seqMapper;
    }

    public void setSeqMapper(SeqMapper seqMapper) {
        this.seqMapper = seqMapper;
    }

    public Connections getConnections() {
        return nettyBootstrap.getConnections();
    }

    public ThreadPoolTaskExecutor getThreadPool() {
        return threadPool;
    }

    public void setThreadPool(ThreadPoolTaskExecutor threadPool) {
        this.threadPool = threadPool;
    }

    public ChannelHandlerCallBack getChannelHandlerCallBack() {
        return nettyBootstrap.getChannelHandlerCallBack();
    }

    /**
     * start p2p service
     *
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void start() throws IOException, ExecutionException, InterruptedException {
        nettyBootstrap.start();
        logger.info(" start p2p service end.");
    }

    /**
     * @param node
     * @param request
     * @return
     */
    public Response sendRequest(Node node, Request request) {

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
                if (logger.isDebugEnabled()) {
                    logger.debug(" callback: request response {}", response);
                }
                semaphore.release();
            }

            @Override
            public boolean needOnResponse() {
                return true;
            }
        }

        Callback callback = new Callback();
        asyncSendRequest(node, request, callback);

        try {
            callback.semaphore.acquire(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return callback.response;
    }

    /**
     * @param node
     * @param request
     * @param callback
     */
    public void asyncSendRequest(Node node, Request request, ResponseCallBack callback) {
        Message message = Message.builder(request.getType(), request.getContent());

        callback.setMessage(message);
        callback.setSeqMapper(getSeqMapper());

        String nodeID = node.getNodeID();
        if (logger.isTraceEnabled()) {
            logger.trace(
                    " request content, node: {}, seq: {}, type: {}, timeout: {}, content: {}",
                    nodeID,
                    message.getSeq(),
                    message.getType(),
                    request.getTimeout(),
                    request.getContent());
        }

        // select random nodes to send
        ChannelHandlerContext ctx = getConnections().getChannelHandler(nodeID);
        if (ctx != null && ctx.channel().isActive()) {
            callback.setCtx(ctx);
            getSeqMapper().add(message.getSeq(), callback);

            if (request.getTimeout() > 0) {

                final ResponseCallBack finalCallback = callback;
                final ThreadPoolTaskExecutor finalThreadPool = threadPool;
                if (callback.needOnResponse()) {
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
                                                                        StatusCode.TIMEOUT,
                                                                        "timeout");
                                                            }
                                                        });
                                            }
                                        }
                                    },
                                    request.getTimeout(),
                                    TimeUnit.MILLISECONDS));
                }
            }

            MessageSerializer serializer = new MessageSerializer();
            ByteBuf byteBuf = ctx.alloc().buffer();
            serializer.serialize(message, byteBuf);
            ctx.writeAndFlush(byteBuf);
            if (logger.isTraceEnabled()) {
                logger.trace(" send request, host: {}, seq: {}", node, message.getSeq());
            }
        } else {
            callback.sendFailed(StatusCode.UNREACHABLE, "node unreachable");
        }
    }
}
