package com.webank.wecross.test.host;

import com.webank.wecross.Application;
import com.webank.wecross.host.Peer;
import com.webank.wecross.host.PeerManager;
import com.webank.wecross.network.NetworkManager;
import java.util.Set;
import javax.annotation.Resource;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PeerManagerTest {

    @Resource(name = "newPeerManager")
    PeerManager peerManager;

    @Resource(name = "")
    NetworkManager networkManager;

    @BeforeClass
    public static void runApp() {
        // Start the application to mock remote WeCross host
        Application.main(new String[] {});
    }

    @AfterClass
    public static void afterclass() {
        // do nothing

    }

    @Test
    public void a() // first to test
            {
        // Check configure loading
        System.out.println("Peer size: " + peerManager.peerSize());
        Assert.assertEquals(2, peerManager.peerSize());
        Assert.assertTrue(peerManager.getPeer("127.0.0.1:8081") != null);
        Assert.assertTrue(peerManager.getPeer("127.0.0.1:8082") != null);

        // Remove mock peers
        peerManager.clearPeers();
        Assert.assertEquals(0, peerManager.peerSize());
    }

    @Test
    public void syncByRequestPeerInfo() throws Exception {
        // Remove mock peers
        peerManager.clearPeers();
        Assert.assertEquals(0, peerManager.peerSize());

        // check all syncing
        Peer peer = new Peer("127.0.0.1:8080", "myself");
        peerManager.updatePeer(peer);

        peerManager.broadcastPeerInfoRequest();
        Thread.sleep(2000); // waiting for syncing

        Set<String> resources = peerManager.getAllPeerResource();
        System.out.println(resources);
        System.out.println(networkManager.getAllNetworkStubResourceName(true));

        Assert.assertTrue(0 < resources.size());
        Assert.assertEquals(networkManager.getAllNetworkStubResourceName(true), resources);

        peerManager.clearPeers();
    }

    @Test
    public void syncByRequestSeq() throws Exception {
        // Remove mock peers
        peerManager.clearPeers();
        Assert.assertEquals(0, peerManager.peerSize());

        // check all syncing
        Peer peer = new Peer("127.0.0.1:8080", "myself");
        peerManager.updatePeer(peer);

        peerManager.broadcastSeqRequest();
        Thread.sleep(2000); // waiting for syncing

        Set<String> resources = peerManager.getAllPeerResource();
        System.out.println(resources);
        System.out.println(networkManager.getAllNetworkStubResourceName(true));

        Assert.assertTrue(0 < resources.size());
        Assert.assertEquals(networkManager.getAllNetworkStubResourceName(true), resources);

        peerManager.clearPeers();
    }
}
