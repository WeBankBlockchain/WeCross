package com.webank.wecross.p2p.netty;

import com.webank.wecross.p2p.netty.common.Node;
import io.netty.channel.ChannelHandlerContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Connections {

    private static final Logger logger = LoggerFactory.getLogger(Connections.class);

    public Set<Node> getConfiguredPeers() {
        return configuredPeers;
    }

    public void setConfiguredPeers(Set<Node> configuredPeers) {
        this.configuredPeers = configuredPeers;
    }

    /** all Peers should connect */
    private Set<Node> configuredPeers;

    /** nodeID => ChannelHandlerContext */
    private Map<String, ChannelHandlerContext> nodeID2ChannelHandler = new HashMap<>();
    /** Peer Host => nodeID */
    private Map<Node, String> host2NodeID = new HashMap<>();

    public Map<String, ChannelHandlerContext> getNodeID2ChannelHandler() {
        return nodeID2ChannelHandler;
    }

    public void setNodeID2ChannelHandler(Map<String, ChannelHandlerContext> nodeID2ChannelHandler) {
        this.nodeID2ChannelHandler = nodeID2ChannelHandler;
    }

    public Map<Node, String> getHost2NodeID() {
        return host2NodeID;
    }

    public void setHost2NodeID(Map<Node, String> host2NodeID) {
        this.host2NodeID = host2NodeID;
    }

    /**
     * get all should reconnect nodes
     *
     * @return
     */
    public Set<Node> shouldConnectNodes() {

        Set<Node> hostSet = new HashSet<>();
        Set<Node> configuredPeers = getConfiguredPeers();
        for (Node host : configuredPeers) {
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
            Node node, ChannelHandlerContext ctx, boolean connectToServer) {
        int hashCode = System.identityHashCode(ctx);

        logger.info(" add channel handler, node: {}, host: {}, ctx: {}", node.getNodeID(), node, hashCode);

        ChannelHandlerContext oldCtx = null;
        synchronized (nodeID2ChannelHandler) {
            oldCtx = nodeID2ChannelHandler.get(node.getNodeID());

            if (oldCtx == null) {
                nodeID2ChannelHandler.put(node.getNodeID(), ctx);

                synchronized (host2NodeID) {
                    host2NodeID.put(node, node.getNodeID());
                }
            }
        }

        if (oldCtx != null) {
            logger.info(" connection exist, host: {}, node: {} ", node, node.getNodeID());
            if (connectToServer) {
                synchronized (host2NodeID) {
                    for (Map.Entry<Node, String> entry : host2NodeID.entrySet()) {
                        if (entry.getValue().equals(node.getNodeID())) {
                            host2NodeID.remove(entry.getKey());
                            host2NodeID.put(node, node.getNodeID());
                            logger.info(
                                    " update, last host: {}, host: {}, node: {} ",
                                    entry.getKey(),
                                    node,
                                    node.getNodeID());
                            break;
                        }
                    }
                }
            }

            throw new UnsupportedOperationException(" existing connection, node : " + node.getNodeID());
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
    public void removeChannelHandler(Node node, ChannelHandlerContext ctx) {
        int hashCode = System.identityHashCode(ctx);

        logger.info(" remove channel handler, host: {}, node: {}, ctx: {}", node, node.getNodeID(), hashCode);

        ChannelHandlerContext oldCtx = null;
        synchronized (nodeID2ChannelHandler) {
            oldCtx = nodeID2ChannelHandler.get(node.getNodeID());

            if (oldCtx != null && oldCtx == ctx) {
                nodeID2ChannelHandler.remove(node.getNodeID());
                logger.info(" remove channel handler, node: {}", node.getNodeID());
            }
        }

        if (oldCtx != null && oldCtx == ctx) {
            synchronized (host2NodeID) {
                for (Map.Entry<Node, String> entry : host2NodeID.entrySet()) {
                    if (entry.getValue().equals(node.getNodeID())) {
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
                    node,
                    node.getNodeID(),
                    hashCode);
        }
    }
}
