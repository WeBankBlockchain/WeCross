package com.webank.wecross.restserver.p2p;

import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.P2PMessageCallback;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.Peer;

public class RestfulP2PMessageEngine extends P2PMessageEngine {
    @Override
    public <T> void asyncSendMessage(Peer peer, P2PMessage<T> msg, P2PMessageCallback callback) {}
}
