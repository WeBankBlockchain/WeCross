package com.webank.wecross.test.peer;

import com.webank.wecross.network.p2p.netty.common.Node;
import com.webank.wecross.peer.PeerManager;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeerManagerTest {
    private Logger logger = LoggerFactory.getLogger(PeerManagerTest.class);

    @Test
    public void seqTest() {
        PeerManager peerManager = new PeerManager();

        for (int i = 0; i < 100; i++) {
            int prevSeq = peerManager.getSeq();

            peerManager.newSeq();
            int currentSeq = peerManager.getSeq();

            Assert.assertNotEquals(prevSeq, currentSeq);
        }
    }

    @Test
    public void testPeerInfo() {
        PeerManager peerManager = new PeerManager();

        peerManager.addPeerInfo(new Node("node1", "", 8888));
        peerManager.addPeerInfo(new Node("node2", "", 8889));
        peerManager.addPeerInfo(new Node("node3", "", 8890));

        Assert.assertEquals(3, peerManager.getPeerInfos().size());
        Assert.assertNull(peerManager.getPeerInfo(new Node("node3", "", 8811)));

        peerManager.removePeerInfo(new Node("node2", "", 8889));
        peerManager.removePeerInfo(new Node("node1", "", 8889));

        Assert.assertEquals(2, peerManager.peerSize());
        Assert.assertNull(peerManager.getPeerInfo(new Node("node2", "", 8889)));
        Assert.assertNotNull(peerManager.getPeerInfo(new Node("node1", "", 8888)));
    }
}
