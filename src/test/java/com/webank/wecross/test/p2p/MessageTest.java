package com.webank.wecross.test.p2p;

import com.webank.wecross.p2p.Message;
import org.junit.Assert;
import org.junit.Test;

public class MessageTest {
    @Test
    public void allTest() throws Exception {
        String mockBuffer = new String("aabbccddefg");
        Message msg = new Message(mockBuffer);

        Assert.assertSame(msg.getBuffer(), mockBuffer);

        Assert.assertSame(msg.size(), 11);

        int mockSeq = 22333;
        msg.setSeq(mockSeq);
        Assert.assertTrue(msg.getSeq() == mockSeq);

        msg.setBuffer("dddddddddd");
        Assert.assertSame(msg.getBuffer(), "dddddddddd");
    }
}
