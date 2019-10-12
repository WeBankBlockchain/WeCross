package com.webank.wecross.restserver.p2p;

import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.P2PMessageCallback;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.Peer;

public class RestfulP2PMessageEngine implements P2PMessageEngine {
    @Override
    public void asyncSendMessage(Peer peer, P2PMessage msg, P2PMessageCallback callback) {}
}
