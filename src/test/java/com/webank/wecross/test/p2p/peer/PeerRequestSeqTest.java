package com.webank.wecross.test.p2p.peer;

import com.webank.wecross.host.Peer;
import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.peer.PeerRequestSeqMessageCallback;
import com.webank.wecross.p2p.peer.PeerRequestSeqMessageData;
import com.webank.wecross.p2p.peer.PeerSeqMessageData;
import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class PeerRequestSeqTest {
    @Resource(name = "newRestfulP2PMessageEngine")
    private P2PMessageEngine engine;

    class MockPeerRequestSeqMessageCallback extends PeerRequestSeqMessageCallback {
        private Logger logger = LoggerFactory.getLogger(MockPeerRequestSeqMessageCallback.class);

        @Override
        public void onResponse(int status, String message, P2PMessage msg) {
            PeerSeqMessageData data = (PeerSeqMessageData) msg.getData();
            logger.info(
                    "Mock receive peer seq. status: {} message: {} seq: {}",
                    status,
                    message,
                    data == null ? "null" : data.getDataSeq());
            Assert.assertTrue(status == 0);
            Assert.assertTrue(data.getDataSeq() != 0);
        }
    }

    @Test
    public void allTest() throws Exception {

        Peer peer = new Peer("127.0.0.1:8080", "fake peer");

        PeerRequestSeqMessageData data = new PeerRequestSeqMessageData();
        P2PMessage<PeerRequestSeqMessageData> msg = new P2PMessage<PeerRequestSeqMessageData>();
        msg.newSeq();
        msg.setData(data);
        msg.setVersion("0.1");
        msg.setType("peer");

        MockPeerRequestSeqMessageCallback callback = new MockPeerRequestSeqMessageCallback();
        callback.setPeer(peer);

        engine.asyncSendMessage(peer, msg, callback);
    }
}
