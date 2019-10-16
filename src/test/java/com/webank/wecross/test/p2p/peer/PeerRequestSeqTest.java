package com.webank.wecross.test.p2p.peer;

import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.Peer;
import com.webank.wecross.p2p.peer.PeerRequestSeqMessageCallback;
import com.webank.wecross.p2p.peer.PeerRequestSeqMessageData;
import com.webank.wecross.p2p.peer.PeerSeqMessageData;
import com.webank.wecross.restserver.p2p.RestfulP2PMessageEngineFactory;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeerRequestSeqTest {
    class MockPeerRequestSeqMessageCallback extends PeerRequestSeqMessageCallback {
        private Logger logger = LoggerFactory.getLogger(MockPeerRequestSeqMessageCallback.class);

        @Override
        public void onResponse(int status, String message, PeerSeqMessageData data) {

            Assert.assertTrue(status == 0);
        }
    }

    @Test
    public void allTest() throws Exception {
        P2PMessageEngine engine = new RestfulP2PMessageEngineFactory().getEngine();

        Peer peer = new Peer("http://127.0.0.1:8080", "fake peer");

        PeerRequestSeqMessageData data = new PeerRequestSeqMessageData();
        P2PMessage<PeerRequestSeqMessageData> msg = new P2PMessage<PeerRequestSeqMessageData>();
        msg.newSeq();
        msg.setData(data);
        msg.setVersion("0.1");
        msg.setType("peer");

        MockPeerRequestSeqMessageCallback callback = new MockPeerRequestSeqMessageCallback();

        engine.asyncSendMessage(peer, msg, callback);
    }
}