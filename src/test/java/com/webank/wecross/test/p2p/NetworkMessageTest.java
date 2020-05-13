package com.webank.wecross.test.p2p;

import com.webank.wecross.network.NetworkMessage;
import org.junit.Assert;
import org.junit.Test;

public class NetworkMessageTest {
    @Test
    public void allTest() throws Exception {
        String mockBuffer = new String("aabbccddefg");
        NetworkMessage msg = new NetworkMessage();
        msg.setData(mockBuffer);

        Assert.assertSame(msg.getData(), mockBuffer);

        Assert.assertTrue(msg.getSeq() == 0);
        msg.newSeq();
        Assert.assertTrue(msg.getSeq() != 0);

        msg.setData("dddddddddd");
        Assert.assertSame(msg.getData(), "dddddddddd");
    }
}
