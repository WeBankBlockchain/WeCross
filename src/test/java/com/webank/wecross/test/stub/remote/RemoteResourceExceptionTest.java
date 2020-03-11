package com.webank.wecross.test.stub.remote;

import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.netty.common.Node;
import com.webank.wecross.peer.Peer;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.resource.ResourceInfo;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import com.webank.wecross.test.Mock.MockNetworkManagerFactory;
import com.webank.wecross.test.Mock.MockP2PMessageEngineFactory;
import com.webank.wecross.test.Mock.P2PEngineMessageFilter;
import com.webank.wecross.zone.ZoneManager;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;

public class RemoteResourceExceptionTest {

    class AlwaysErrorFilter extends P2PEngineMessageFilter {

        private P2PMessage randToNull(P2PMessage msg) {
            SecureRandom rand = new SecureRandom();
            return rand.nextBoolean() ? null : msg;
        }

        @Override
        public P2PMessage handle1(P2PMessage msg) {
            Assert.assertNotEquals(null, msg);
            Assert.assertTrue("Unsupported method: " + msg.getMethod(), false);
            return null;
        }

        @Override
        public P2PMessage handle4(P2PMessage msg) {
            Assert.assertNotEquals(null, msg);

            try {
                String r[] = msg.getMethod().split("/");
                Path path = new Path();
                path.setNetwork(r[0]);
                path.setChain(r[1]);
                path.setResource(r[2]);

                Resource resource = networkManager.getResource(path);

                switch (r[3]) {
                    case "call":
                        return handleCall(resource, msg);
                    case "sendTransaction":
                        return handleSendTransaction(resource, msg);
                    default:
                        Assert.assertTrue("Unsupported method: " + msg.getMethod(), false);
                }

            } catch (Exception e) {
                Assert.assertTrue(msg.getMethod() + " exception: " + e, false);
            }
            return msg;
        }

        private P2PMessage handleCall(Resource resource, P2PMessage msg) {
            TransactionResponse responseData = resource.call((TransactionRequest) msg.getData());
            responseData.setErrorCode(1); // set error
            P2PMessage<TransactionResponse> response = new P2PMessage<>();
            response.setData(responseData);
            response.setSeq(msg.getSeq());
            return randToNull(response); // random return null
        }

        private P2PMessage handleSendTransaction(Resource resource, P2PMessage msg) {
            TransactionResponse responseData =
                    resource.sendTransaction((TransactionRequest) msg.getData());
            responseData.setErrorCode(1); // set error
            P2PMessage<TransactionResponse> response = new P2PMessage<>();
            response.setData(responseData);
            response.setSeq(msg.getSeq());

            return randToNull(response); // random return null
        }
    }

    private P2PMessageEngine p2pEngine;
    private ZoneManager networkManager;

    public RemoteResourceExceptionTest() {
        p2pEngine = MockP2PMessageEngineFactory.newMockP2PMessageEngine(new AlwaysErrorFilter());
        networkManager = MockNetworkManagerFactory.newMockNteworkManager(p2pEngine);

        try {
            ResourceInfo resourceInfo = new ResourceInfo();
            resourceInfo.setPath("test-network.test-stub.test-local-resource");
            resourceInfo.setDistance(1);

            Set<ResourceInfo> resources = new HashSet<ResourceInfo>();
            resources.add(resourceInfo);

            networkManager.addRemoteResources(new Peer(new Node("", "", 0)), resources);
        } catch (Exception e) {
            Assert.assertTrue("Add test resource exception: " + e, false);
        }
    }

    /*
    @Test
    public void remoteResourceCallTest() throws Exception {
        Peer peer = new Peer(new Node("", "", 0));
        Resource resource = new RemoteResource(peer, 1, p2pEngine);
        resource.setPath(Path.decode("test-network.test-stub.test-local-resource"));

        TransactionRequest request = new TransactionRequest();
        request.setMethod("get");
        request.setArgs(new String[] {"123", "aabb"});

        try {
            for (int i = 0; i < 10; i++) {
                TransactionResponse response = resource.call(request);
                Assert.assertFalse(response.getErrorCode().equals(0));
            }

        } catch (Exception e) {
            Assert.assertTrue("Resource exception", false);
        }
    }

    @Test
    public void remoteResourceSendTransactionTest() throws Exception {
        Peer peer = new Peer(new Node("", "", 0));
        Resource resource = new RemoteResource(peer, 1, p2pEngine);
        resource.setPath(Path.decode("test-network.test-stub.test-local-resource"));

        TransactionRequest request = new TransactionRequest();
        request.setMethod("sendTransaction");
        request.setArgs(new String[] {"123", "aabb"});

        try {
            for (int i = 0; i < 10; i++) {
                TransactionResponse response = resource.sendTransaction(request);
                Assert.assertFalse(response.getErrorCode().equals(0));
            }

        } catch (Exception e) {
            Assert.assertTrue("Resource exception", false);
        }
    }
    */
}
