package com.webank.wecross.test.peer;

import com.webank.wecross.Application;
import com.webank.wecross.network.NetworkManager;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.Peer;
import com.webank.wecross.peer.PeerManager;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.request.TransactionRequest;
import com.webank.wecross.resource.response.TransactionResponse;
import com.webank.wecross.stub.remote.RemoteResource;
import java.util.Set;
import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
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

    @Resource(name = "newNetworkManager")
    NetworkManager networkManager;

    @Resource(name = "newP2PMessageEngine")
    private P2PMessageEngine p2pEngine;

    @BeforeClass
    public static void runApp() {
        // Start the application to mock remote WeCross host
        Application.main(new String[] {});
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

    @Test
    public void remoteResourceCallTest() throws Exception {
        Peer peer = new Peer();
        peer.setUrl("127.0.0.1:8080");
        com.webank.wecross.resource.Resource resource = new RemoteResource(peer, 1, p2pEngine);
        resource.setPath(Path.decode("test-network.test-stub.test-resource"));

        TransactionRequest request = new TransactionRequest();
        request.setMethod("get");
        request.setArgs(new String[] {"123", "aabb"});

        TransactionResponse response = resource.call(request);
        Assert.assertEquals(new Integer(0), response.getErrorCode());
        System.out.println(response.getErrorMessage());
    }

    @Test
    public void remoteResourceSendTransactionTest() throws Exception {
        Peer peer = new Peer();
        peer.setUrl("127.0.0.1:8080");
        com.webank.wecross.resource.Resource resource = new RemoteResource(peer, 1, p2pEngine);
        resource.setPath(Path.decode("test-network.test-stub.test-resource"));

        TransactionRequest request = new TransactionRequest();
        request.setMethod("sendTransaction");
        request.setArgs(new String[] {"123", "aabb"});

        TransactionResponse response = resource.call(request);
        Assert.assertEquals(new Integer(0), response.getErrorCode());
        System.out.println(response.getErrorMessage());
    }
}
