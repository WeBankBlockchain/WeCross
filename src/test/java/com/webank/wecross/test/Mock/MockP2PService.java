package com.webank.wecross.test.Mock;

import com.webank.wecross.network.NetworkCallback;
import com.webank.wecross.network.NetworkMessage;
import com.webank.wecross.network.NetworkResponse;
import com.webank.wecross.network.p2p.P2PService;
import com.webank.wecross.peer.Peer;
import org.junit.Assert;

public class MockP2PService extends P2PService {

    private P2PEngineMessageFilter filter;

    public MockP2PService(P2PEngineMessageFilter filter) {
        this.filter = filter;
    }

    public <T> void asyncSendMessage(
            Peer peer, NetworkMessage<T> msg, NetworkCallback<?> callback) {
        try {
            Assert.assertNotEquals(null, filter); // Please set filter beforehand

            checkP2PMessage(msg);
            checkCallback(callback);

            NetworkResponse response = filter.checkAndResponse(msg);

            executeCallback(callback, 0, "Success", response);
        } catch (Exception e) {
            executeCallback(callback, -1, "Error", null);
        }
    }

    public void setFilter(P2PEngineMessageFilter filter) {
        this.filter = filter;
    }
}
