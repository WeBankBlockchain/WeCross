package com.webank.wecross.test.peer;

import com.webank.wecross.network.NetworkManager;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.netty.common.Peer;
import com.webank.wecross.peer.PeerInfo;
import com.webank.wecross.peer.PeerManager;
import com.webank.wecross.resource.Path;
import com.webank.wecross.restserver.request.TransactionRequest;
import com.webank.wecross.restserver.response.TransactionResponse;
import com.webank.wecross.stub.remote.RemoteResource;
import java.util.Set;
import javax.annotation.Resource;
import org.junit.*;

// @RunWith(SpringRunner.class)
// @SpringBootTest
// @FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PeerManagerTest {

    @Resource(name = "newPeerManager")
    PeerManager peerManager;

    @Resource(name = "newNetworkManager")
    NetworkManager networkManager;

    @Resource(name = "newP2PMessageEngine")
    private P2PMessageEngine p2pEngine;

    // @Test
    public void syncByRequestPeerInfo() throws Exception {
        // Remove mock peers
        peerManager.clearPeerInfos();
        Assert.assertEquals(0, peerManager.peerSize());

        // check all syncing
        PeerInfo peerInfo = new PeerInfo(new Peer());
        peerManager.updatePeerInfo(peerInfo);

        peerManager.broadcastPeerInfoRequest();
        Thread.sleep(2000); // waiting for syncing

        Set<String> resources = peerManager.getAllPeerResource();
        System.out.println(resources);
        System.out.println(networkManager.getAllNetworkStubResourceName(true));

        Assert.assertTrue(0 < resources.size());
        Assert.assertEquals(networkManager.getAllNetworkStubResourceName(true), resources);

        peerManager.clearPeerInfos();
    }

    // @Test
    public void syncByRequestSeq() throws Exception {
        // Remove mock peers
        peerManager.clearPeerInfos();
        Assert.assertEquals(0, peerManager.peerSize());

        // check all syncing
        PeerInfo peerInfo = new PeerInfo(new Peer());
        peerManager.updatePeerInfo(peerInfo);

        peerManager.broadcastSeqRequest();
        Thread.sleep(2000); // waiting for syncing

        Set<String> resources = peerManager.getAllPeerResource();
        System.out.println(resources);
        System.out.println(networkManager.getAllNetworkStubResourceName(true));

        Assert.assertTrue(0 < resources.size());
        Assert.assertEquals(networkManager.getAllNetworkStubResourceName(true), resources);

        peerManager.clearPeerInfos();
    }

    // @Test
    public void remoteResourceCallTest() throws Exception {
        Peer peer = new Peer();
        com.webank.wecross.resource.Resource resource = new RemoteResource(peer, 1, p2pEngine);
        resource.setPath(Path.decode("test-network.test-stub.test-resource"));

        TransactionRequest request = new TransactionRequest();
        request.setMethod("get");
        request.setArgs(new String[] {"123", "aabb"});

        TransactionResponse response = resource.call(request);
        Assert.assertEquals(new Integer(0), response.getErrorCode());
        System.out.println(response.getErrorMessage());
    }

    // @Test
    public void remoteResourceSendTransactionTest() throws Exception {
        Peer peer = new Peer();
        com.webank.wecross.resource.Resource resource = new RemoteResource(peer, 1, p2pEngine);
        resource.setPath(Path.decode("test-network.test-stub.test-resource"));

        TransactionRequest request = new TransactionRequest();
        request.setMethod("sendTransaction");
        request.setArgs(new String[] {"123", "aabb"});

        TransactionResponse response = resource.sendTransaction(request);
        Assert.assertEquals(new Integer(0), response.getErrorCode());
        System.out.println(response.getErrorMessage());
    }
}
