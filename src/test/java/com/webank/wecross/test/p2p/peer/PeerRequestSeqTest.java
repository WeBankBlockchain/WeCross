package com.webank.wecross.test.p2p.peer;

import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.Peer;
import com.webank.wecross.p2p.peer.PeerRequestSeqMessageCallback;
import com.webank.wecross.p2p.peer.PeerRequestSeqMessageData;
import com.webank.wecross.restserver.p2p.RestfulP2PMessageEngineFactory;
import org.junit.Test;

public class PeerRequestSeqTest {
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

        PeerRequestSeqMessageCallback callback = new PeerRequestSeqMessageCallback();

        engine.asyncSendMessage(peer, msg, callback);
    }
}
