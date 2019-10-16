package com.webank.wecross.test.host;

import com.webank.wecross.host.PeerManager;
import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class PeerManagerTest {
    @Resource(name = "newPeerManager")
    PeerManager peerManager;

    @Test
    public void loadConfigTest() {
        System.out.println("Peer size: " + peerManager.peerSize());
        Assert.assertSame(2, peerManager.peerSize());
    }
}
