package com.webank.wecross.p2p;

import com.webank.wecross.host.Peer;

public abstract class P2PMessageEngine {
    public abstract <T> void asyncSendMessage(
            Peer peer, P2PMessage<T> msg, P2PMessageCallback callback);
}
