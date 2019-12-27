package com.webank.wecross.test.p2p;

import com.webank.wecross.p2p.P2PMessage;
import org.junit.Assert;
import org.junit.Test;

public class P2PMessageTest {
    @Test
    public void allTest() throws Exception {
        String mockBuffer = new String("aabbccddefg");
        P2PMessage msg = new P2PMessage();
        msg.setData(mockBuffer);

        Assert.assertSame(msg.getData(), mockBuffer);

        Assert.assertTrue(msg.getSeq() == 0);
        msg.newSeq();
        Assert.assertTrue(msg.getSeq() != 0);

        msg.setData("dddddddddd");
        Assert.assertSame(msg.getData(), "dddddddddd");
    }
}
