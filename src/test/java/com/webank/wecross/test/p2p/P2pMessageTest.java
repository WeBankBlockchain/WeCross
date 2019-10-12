package com.webank.wecross.test.p2p;

import com.webank.wecross.p2p.P2PMessage;
import org.junit.Assert;
import org.junit.Test;

public class P2pMessageTest {
    @Test
    public void allTest() throws Exception {
        String mockBuffer = new String("aabbccddefg");
        P2PMessage msg = new P2PMessage(mockBuffer);

        Assert.assertSame(msg.getBuffer(), mockBuffer);

        Assert.assertSame(msg.size(), 11);

        int mockSeq = P2PMessage.newSeq();
        msg.setSeq(mockSeq);
        Assert.assertTrue(msg.getSeq() == mockSeq);
        Assert.assertTrue(msg.getSeq() != P2PMessage.newSeq());

        msg.setBuffer("dddddddddd");
        Assert.assertSame(msg.getBuffer(), "dddddddddd");
    }
}
