package com.webank.wecross.p2p;

public interface P2PMessageEngine {
    public void asyncSendMessage(Peer peer, P2PMessage msg, P2PMessageCallback callback);
}
