package com.webank.wecross.p2p.netty;

import com.webank.wecross.p2p.netty.common.Host;
import com.webank.wecross.p2p.netty.common.Peer;
import com.webank.wecross.p2p.netty.common.Utils;
import io.netty.channel.ChannelHandlerContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Connections {

    private static final Logger logger = LoggerFactory.getLogger(Connections.class);

    public Set<Host> getConfiguredPeers() {
        return configuredPeers;
    }

    public void setConfiguredPeers(Set<Host> configuredPeers) {
        this.configuredPeers = configuredPeers;
    }

    /** all Peers should connect */
    private Set<Host> configuredPeers;

    /** nodeID => ChannelHandlerContext */
    private Map<String, ChannelHandlerContext> nodeID2ChannelHandler = new HashMap<>();
    /** Peer Host => nodeID */
    private Map<Host, String> host2NodeID = new HashMap<>();

    public Map<String, ChannelHandlerContext> getNodeID2ChannelHandler() {
        return nodeID2ChannelHandler;
    }

    public void setNodeID2ChannelHandler(Map<String, ChannelHandlerContext> nodeID2ChannelHandler) {
        this.nodeID2ChannelHandler = nodeID2ChannelHandler;
    }

    public Map<Host, String> getHost2NodeID() {
        return host2NodeID;
    }

    public void setHost2NodeID(Map<Host, String> host2NodeID) {
        this.host2NodeID = host2NodeID;
    }

    /**
     * get all should reconnect nodes
     *
     * @return
     */
    public Set<Host> shouldConnectNodes() {

        Set<Host> hostSet = new HashSet<>();
        Set<Host> configuredPeers = getConfiguredPeers();
        for (Host host : configuredPeers) {
            synchronized (host2NodeID) {
                if (host2NodeID.containsKey(host)) {
                    continue;
                }
            }
            hostSet.add(host);
        }

        return hostSet;
    }

    /**
     * get all active channel handler context
     *
     * @return
     */
    public List<ChannelHandlerContext> activeChannelHandlers() {

        List<ChannelHandlerContext> result = new ArrayList<>();
        synchronized (nodeID2ChannelHandler) {
            for (Map.Entry<String, ChannelHandlerContext> entry :
                    nodeID2ChannelHandler.entrySet()) {
                if (entry.getValue().channel().isActive()) {
                    result.add(entry.getValue());
                } else {
                    logger.warn(
                            " channel handler not active ??? node: {}, ctx: {}",
                            entry.getKey(),
                            System.identityHashCode(entry.getValue()));
                }
            }
        }

        return result;
    }

    /**
     * @param nodeID
     * @param ctx
     * @return
     */
    public void addChannelHandler(
            String nodeID, ChannelHandlerContext ctx, boolean connectToServer) {
        Host host = Utils.channelContextPeerHost(ctx);
        int hashCode = System.identityHashCode(ctx);

        logger.info(" add channel handler, node: {}, host: {}, ctx: {}", nodeID, host, hashCode);

        ChannelHandlerContext oldCtx = null;
        synchronized (nodeID2ChannelHandler) {
            oldCtx = nodeID2ChannelHandler.get(nodeID);

            if (oldCtx == null) {
                nodeID2ChannelHandler.put(nodeID, ctx);

                synchronized (host2NodeID) {
                    host2NodeID.put(host, nodeID);
                }
            }
        }

        if (oldCtx != null) {
            logger.info(" connection exist, host: {}, node: {} ", host, nodeID);
            if (connectToServer) {
                synchronized (host2NodeID) {
                    for (Map.Entry<Host, String> entry : host2NodeID.entrySet()) {
                        if (entry.getValue().equals(nodeID)) {
                            host2NodeID.remove(entry.getKey());
                            host2NodeID.put(host, nodeID);
                            logger.info(
                                    " update, last host: {}, host: {}, node: {} ",
                                    entry.getKey(),
                                    host,
                                    nodeID);
                            break;
                        }
                    }
                }
            }

            throw new UnsupportedOperationException(" existing connection, node : " + nodeID);
        }
    }

    /**
     * @param nodeID
     * @return
     */
    public ChannelHandlerContext getChannelHandler(String nodeID) {
        ChannelHandlerContext ctx = null;
        synchronized (nodeID2ChannelHandler) {
            ctx = nodeID2ChannelHandler.get(nodeID);
        }

        return ctx;
    }

    /**
     * @param nodeID
     * @param ctx
     */
    public void removeChannelHandler(String nodeID, ChannelHandlerContext ctx) {
        Host host = Utils.channelContextPeerHost(ctx);
        int hashCode = System.identityHashCode(ctx);

        logger.info(" remove channel handler, host: {}, node: {}, ctx: {}", host, nodeID, hashCode);

        ChannelHandlerContext oldCtx = null;
        synchronized (nodeID2ChannelHandler) {
            oldCtx = nodeID2ChannelHandler.get(nodeID);

            if (oldCtx != null && oldCtx == ctx) {
                nodeID2ChannelHandler.remove(nodeID);
                logger.info(" remove channel handler, node: {}", nodeID);
            }
        }

        if (oldCtx != null && oldCtx == ctx) {
            synchronized (host2NodeID) {
                for (Map.Entry<Host, String> entry : host2NodeID.entrySet()) {
                    if (entry.getValue().equals(nodeID)) {
                        logger.info(
                                " remove host info, host: {}, node: {} ",
                                entry.getKey(),
                                entry.getValue());
                        host2NodeID.remove(entry.getKey());
                        break;
                    }
                }
            }
        }

        if (oldCtx == null) {
            logger.warn(
                    " channel handler not exist, host: {}, node: {}, ctx: {}",
                    host,
                    nodeID,
                    hashCode);
        }
    }

    public Set<Peer> getPeers() {
        Set<Peer> setPeer = new HashSet<Peer>();
        synchronized (nodeID2ChannelHandler) {
            for (Map.Entry<String, ChannelHandlerContext> entry :
                    nodeID2ChannelHandler.entrySet()) {
                setPeer.add(new Peer(entry.getKey()));
            }
        }

        return setPeer;
    }
}
