package com.webank.wecross.test.peer;

import com.webank.wecross.network.NetworkManager;
import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.netty.P2PService;
import com.webank.wecross.p2p.netty.common.Peer;
import com.webank.wecross.peer.PeerInfo;
import com.webank.wecross.peer.PeerInfoMessageData;
import com.webank.wecross.peer.PeerManager;
import com.webank.wecross.peer.PeerSeqMessageData;
import com.webank.wecross.resource.ResourceInfo;
import com.webank.wecross.test.Mock.MockNetworkManagerFactory;
import com.webank.wecross.test.Mock.MockP2PMessageEngineFactory;
import com.webank.wecross.test.Mock.MockP2PService;
import com.webank.wecross.test.Mock.MockPeerManagerFactory;
import com.webank.wecross.test.Mock.P2PEngineMessageFilter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeerManagerTest {
    private Logger logger = LoggerFactory.getLogger(PeerManagerTest.class);

    class PeerTestEngineFilter extends P2PEngineMessageFilter {

        @Override
        public P2PMessage handle1(P2PMessage msg) {
            String method = msg.getMethod();
            switch (method) {
                case "requestSeq":
                    return handleRequestSeq(msg);
                case "seq":
                    return handleSeq(msg);
                case "requestPeerInfo":
                    return handleRequestPeerInfo(msg);
                case "peerInfo":
                    return handlePeerInfo(msg);
                default:
                    Assert.assertTrue("Unsupported method" + method, false);
            }

            return null;
        }

        @Override
        public P2PMessage handle4(P2PMessage msg) {
            Assert.assertTrue("Unsupported method" + msg.getMethod(), false);
            return null;
        }

        private P2PMessage handleRequestSeq(P2PMessage msg) {
            Assert.assertEquals("requestSeq", msg.getMethod());
            Assert.assertNotEquals(0, msg.getSeq());

            PeerSeqMessageData data = new PeerSeqMessageData();
            data.setSeq(12345);
            P2PMessage<PeerSeqMessageData> response = new P2PMessage<>();
            response.setData(data);

            return response;
        }

        private P2PMessage handleSeq(P2PMessage msg) {
            Assert.assertEquals("seq", msg.getMethod());
            return null;
        }

        private P2PMessage handleRequestPeerInfo(P2PMessage msg) {
            Assert.assertEquals("requestPeerInfo", msg.getMethod());
            Assert.assertNotEquals(0, msg.getSeq());

            Set<ResourceInfo> activeResources = new HashSet<>();
            activeResources.add(new ResourceInfo("network.stub.resource0"));
            activeResources.add(new ResourceInfo("network.stub.resource1"));
            activeResources.add(new ResourceInfo("network.stub.resource2"));

            PeerInfoMessageData data = new PeerInfoMessageData();
            data.setSeq(12345);
            data.setResources(activeResources);

            P2PMessage<PeerInfoMessageData> response = new P2PMessage<>();
            response.setData(data);

            return response;
        }

        private P2PMessage handlePeerInfo(P2PMessage msg) {
            Assert.assertEquals("peerInfo", msg.getMethod());
            return null;
        }
    }

    public PeerManager newMockPeerManager() {
        P2PMessageEngine p2pEngine =
                MockP2PMessageEngineFactory.newMockP2PMessageEngine(new PeerTestEngineFilter());
        NetworkManager networkManager = MockNetworkManagerFactory.newMockNteworkManager(p2pEngine);
        P2PService p2pService = new MockP2PService();
        ((MockP2PService) p2pService).addPeer(new Peer("abcdefg000000"));
        ((MockP2PService) p2pService).addPeer(new Peer("abcdefg111111"));
        PeerManager peerManager =
                MockPeerManagerFactory.newMockPeerManager(networkManager, p2pService, p2pEngine);
        peerManager.maintainPeerConnections();

        return peerManager;
    }

    @Test
    public void seqTest() {
        PeerManager peerManager = newMockPeerManager();

        for (int i = 0; i < 100; i++) {
            int prevSeq = peerManager.getSeq();

            peerManager.newSeq();
            int currentSeq = peerManager.getSeq();

            Assert.assertNotEquals(prevSeq, currentSeq);
        }
    }

    @Test
    public void syncByRequestPeerInfo() throws Exception {
        PeerManager peerManager = newMockPeerManager();

        // Remove mock peers
        peerManager.clearPeerInfos();
        Assert.assertEquals(0, peerManager.peerSize());

        // check all syncing
        PeerInfo peerInfo = new PeerInfo(new Peer("1111111111111111"));
        peerManager.updatePeerInfo(peerInfo);

        peerManager.broadcastPeerInfoRequest();
        Thread.sleep(500); // waiting for syncing
        peerManager.syncWithPeerNetworks();

        Map<String, ResourceInfo> resources =
                peerManager.getNetworkManager().getAllNetworkStubResourceInfo(false);
        System.out.println(resources);

        Assert.assertTrue(0 < resources.size());

        Set<String> paths = new HashSet<>();
        for (ResourceInfo info : resources.values()) {
            paths.add(info.getPath());
        }

        Assert.assertTrue(paths.contains("network.stub.resource0"));
        Assert.assertTrue(paths.contains("network.stub.resource1"));
        Assert.assertTrue(paths.contains("network.stub.resource2"));

        peerManager.clearPeerInfos();
    }

    @Test
    public void syncByRequestSeq() throws Exception {
        PeerManager peerManager = newMockPeerManager();

        // Remove mock peers
        peerManager.clearPeerInfos();
        Assert.assertEquals(0, peerManager.peerSize());

        // check all syncing
        PeerInfo peerInfo = new PeerInfo(new Peer("1111111111111111"));
        peerManager.updatePeerInfo(peerInfo);

        peerManager.broadcastSeqRequest();
        Thread.sleep(500); // waiting for syncing
        peerManager.syncWithPeerNetworks();

        Map<String, ResourceInfo> resources =
                peerManager.getNetworkManager().getAllNetworkStubResourceInfo(false);
        System.out.println(resources);

        Assert.assertTrue(0 < resources.size());

        Set<String> paths = new HashSet<>();
        for (ResourceInfo info : resources.values()) {
            paths.add(info.getPath());
        }

        Assert.assertTrue(paths.contains("network.stub.resource0"));
        Assert.assertTrue(paths.contains("network.stub.resource1"));
        Assert.assertTrue(paths.contains("network.stub.resource2"));

        peerManager.clearPeerInfos();
    }
}
