package com.webank.wecross.test.p2p;

import com.webank.wecross.p2p.Peer;
import org.junit.Assert;
import org.junit.Test;

public class PeerTest {
    @Test
    public void allTest() throws Exception {
        String url = "127.0.0.1:8080/mockpeer";
        String name = "mockPeer";
        Peer peer = new Peer("127.0.0.1:8080/aabbcc", "mockPeer123");

        peer.setName(name);
        Assert.assertSame(peer.getName(), name);

        peer.setUrl(url);
        Assert.assertSame(peer.getUrl(), url);
    }
}
