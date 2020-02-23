package com.webank.wecross.p2p;

import com.webank.wecross.p2p.netty.common.Node;
import com.webank.wecross.p2p.netty.message.processor.Processor;
import com.webank.wecross.p2p.netty.message.proto.Message;
import com.webank.wecross.peer.PeerManager;
import io.netty.channel.ChannelHandlerContext;

public class DisconnectProcessor implements Processor {
    private PeerManager peerManager;

    @Override
    public String name() {
        return "DisconnectProcessor";
    }

    @Override
    public void process(ChannelHandlerContext ctx, Node node, Message message) {
        peerManager.removePeerInfo(node);
    }

    public PeerManager getPeerManager() {
        return peerManager;
    }

    public void setPeerManager(PeerManager peerManager) {
        this.peerManager = peerManager;
    }
}
