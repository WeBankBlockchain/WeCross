package com.webank.wecross.test.stub.remote;

import com.webank.wecross.network.NetworkManager;
import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.netty.common.Node;
import com.webank.wecross.peer.PeerInfo;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.resource.TestResource;
import com.webank.wecross.restserver.request.GetDataRequest;
import com.webank.wecross.restserver.request.SetDataRequest;
import com.webank.wecross.restserver.request.TransactionRequest;
import com.webank.wecross.restserver.response.GetDataResponse;
import com.webank.wecross.restserver.response.SetDataResponse;
import com.webank.wecross.restserver.response.TransactionResponse;
import com.webank.wecross.stub.remote.RemoteResource;
import com.webank.wecross.test.Mock.MockNetworkManagerFactory;
import com.webank.wecross.test.Mock.MockP2PMessageEngineFactory;
import com.webank.wecross.test.Mock.P2PEngineMessageFilter;
import org.junit.Assert;
import org.junit.Test;

public class RemoteResourceTest {

    class RemoteResourceEngineFilter extends P2PEngineMessageFilter {

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
                    case "getData":
                        return handleGetData(resource, msg);
                    case "setData":
                        return handleSetData(resource, msg);
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

        private P2PMessage handleGetData(Resource resource, P2PMessage msg) {
            GetDataResponse responseData = resource.getData((GetDataRequest) msg.getData());
            P2PMessage<GetDataResponse> response = new P2PMessage<>();
            response.setData(responseData);
            response.setSeq(msg.getSeq());
            return response;
        }

        private P2PMessage handleSetData(Resource resource, P2PMessage msg) {
            SetDataResponse responseData = resource.setData((SetDataRequest) msg.getData());
            P2PMessage<SetDataResponse> response = new P2PMessage<>();
            response.setData(responseData);
            response.setSeq(msg.getSeq());
            return response;
        }

        private P2PMessage handleCall(Resource resource, P2PMessage msg) {
            TransactionResponse responseData = resource.call((TransactionRequest) msg.getData());
            P2PMessage<TransactionResponse> response = new P2PMessage<>();
            response.setData(responseData);
            response.setSeq(msg.getSeq());

            return response;
        }

        private P2PMessage handleSendTransaction(Resource resource, P2PMessage msg) {
            TransactionResponse responseData =
                    resource.sendTransaction((TransactionRequest) msg.getData());

            P2PMessage<TransactionResponse> response = new P2PMessage<>();
            response.setData(responseData);
            response.setSeq(msg.getSeq());

            return response;
        }
    }

    private P2PMessageEngine p2pEngine;
    private NetworkManager networkManager;

    public RemoteResourceTest() {
        p2pEngine =
                MockP2PMessageEngineFactory.newMockP2PMessageEngine(
                        new RemoteResourceEngineFilter());
        networkManager = MockNetworkManagerFactory.newMockNteworkManager(p2pEngine);

        try {
            Path path = Path.decode("test-network.test-stub.test-local-resource");
            Resource resource = new TestResource();
            resource.setPath(path);
            networkManager.addResource(resource);
        } catch (Exception e) {
            Assert.assertTrue("Add test resource exception: " + e, false);
        }
    }

    @Test
    public void remoteResourceGetDataTest() throws Exception {
        PeerInfo peer = new PeerInfo(new Node("", "", 0));
        com.webank.wecross.resource.Resource resource = new RemoteResource(peer, 1, p2pEngine);
        resource.setPath(Path.decode("test-network.test-stub.test-local-resource"));

        GetDataRequest request = new GetDataRequest();
        request.setKey("mockKey");

        GetDataResponse response = resource.getData(request);
        Assert.assertEquals(new Integer(0), response.getErrorCode());
        Assert.assertTrue(response.getErrorMessage().contains("getData test resource success"));
        Assert.assertEquals(request.toString(), response.getValue());
        System.out.println(response.getErrorMessage());
    }

    @Test
    public void remoteResourceSetDataTest() throws Exception {
        PeerInfo peer = new PeerInfo(new Node("", "", 0));
        com.webank.wecross.resource.Resource resource = new RemoteResource(peer, 1, p2pEngine);
        resource.setPath(Path.decode("test-network.test-stub.test-local-resource"));

        SetDataRequest request = new SetDataRequest();
        request.setKey("mockKey");
        request.setKey("mockValue");

        SetDataResponse response = resource.setData(request);
        Assert.assertEquals(new Integer(0), response.getErrorCode());
        Assert.assertTrue(response.getErrorMessage().contains("setData test resource success"));
        System.out.println(response.getErrorMessage());
    }

    @Test
    public void remoteResourceCallTest() throws Exception {
        PeerInfo peer = new PeerInfo(new Node("", "", 0));
        com.webank.wecross.resource.Resource resource = new RemoteResource(peer, 1, p2pEngine);
        resource.setPath(Path.decode("test-network.test-stub.test-local-resource"));

        TransactionRequest request = new TransactionRequest();
        request.setMethod("get");
        request.setArgs(new String[] {"123", "aabb"});

        TransactionResponse response = resource.call(request);
        Assert.assertEquals(new Integer(0), response.getErrorCode());
        Assert.assertTrue(response.getErrorMessage().contains("call test resource success"));
        Assert.assertEquals(request, response.getResult()[0]);
        System.out.println(response.getErrorMessage());
    }

    @Test
    public void remoteResourceSendTransactionTest() throws Exception {
        PeerInfo peer = new PeerInfo(new Node("", "", 0));
        com.webank.wecross.resource.Resource resource = new RemoteResource(peer, 1, p2pEngine);
        resource.setPath(Path.decode("test-network.test-stub.test-local-resource"));

        TransactionRequest request = new TransactionRequest();
        request.setMethod("sendTransaction");
        request.setArgs(new String[] {"123", "aabb"});

        TransactionResponse response = resource.sendTransaction(request);
        Assert.assertEquals(new Integer(0), response.getErrorCode());
        Assert.assertTrue(
                response.getErrorMessage().contains("sendTransaction test resource success"));
        Assert.assertEquals(request, response.getResult()[0]);
        System.out.println(response.getErrorMessage());
    }
}
