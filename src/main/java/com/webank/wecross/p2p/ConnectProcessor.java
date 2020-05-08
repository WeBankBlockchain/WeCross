package com.webank.wecross.p2p;

import com.webank.wecross.p2p.netty.common.Node;
import com.webank.wecross.p2p.netty.message.processor.Processor;
import com.webank.wecross.p2p.netty.message.proto.Message;
import com.webank.wecross.peer.PeerManager;
import io.netty.channel.ChannelHandlerContext;

public class ConnectProcessor implements Processor {
    private PeerManager peerManager;

    @Override
    public String name() {
        return "ConnectProcessor";
    }

    @Override
    public void process(ChannelHandlerContext ctx, Node node, Message message) {
        peerManager.addPeerInfo(node);
    }

    public PeerManager getPeerManager() {
        return peerManager;
    }

    public void setPeerManager(PeerManager peerManager) {
        this.peerManager = peerManager;
    }
}
