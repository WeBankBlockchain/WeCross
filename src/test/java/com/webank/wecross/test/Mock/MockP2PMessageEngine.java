package com.webank.wecross.test.Mock;

import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.P2PMessageCallback;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.peer.Peer;

import org.junit.Assert;

public class MockP2PMessageEngine extends P2PMessageEngine {

    private P2PEngineMessageFilter filter;

    public MockP2PMessageEngine(P2PEngineMessageFilter filter) {
        this.filter = filter;
    }

    public <T> void asyncSendMessage(Peer peer, P2PMessage<T> msg, P2PMessageCallback<?> callback) {
        try {
            Assert.assertNotEquals(null, filter); // Please set filter beforehand

            checkP2PMessage(msg);
            checkCallback(callback);

            P2PMessage response = filter.checkAndResponse(msg);

            executeCallback(callback, 0, "Success", response);
        } catch (Exception e) {
            executeCallback(callback, -1, "Error", null);
        }
    }

    public void setFilter(P2PEngineMessageFilter filter) {
        this.filter = filter;
    }
}
