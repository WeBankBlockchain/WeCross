package com.webank.wecross.test.stub.remote;

/*
import com.webank.wecross.p2p.P2PMessage;
import com.webank.wecross.p2p.P2PMessageCallback;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.engine.RestfulP2PMessageEngine;
import com.webank.wecross.p2p.netty.common.Node;
import com.webank.wecross.peer.Peer;
import com.webank.wecross.resource.Path;
import com.webank.wecross.restserver.request.GetDataRequest;
import com.webank.wecross.restserver.request.SetDataRequest;
import com.webank.wecross.restserver.request.TransactionRequest;
import com.webank.wecross.restserver.response.GetDataResponse;
import com.webank.wecross.restserver.response.SetDataResponse;
import com.webank.wecross.restserver.response.TransactionResponse;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class RemoteResourceTest {
    @Test
    public void remoteResourceGetDataTest() throws Exception {
        Peer peer = new Peer(new Node("", "", 0));
        P2PMessageEngine p2pMessageEngine = Mockito.spy(RestfulP2PMessageEngine.class);
        Mockito.doAnswer(
                        new Answer<Object>() {
                            @Override
                            public Object answer(InvocationOnMock invocation) throws Throwable {
                                Peer argPeer = invocation.getArgument(0);
                                Assert.assertEquals(peer, argPeer);

                                P2PMessage<GetDataRequest> argMessage = invocation.getArgument(1);
                                Assert.assertEquals(
                                        "test-network/test-stub/test-local-resource/getData",
                                        argMessage.getMethod());
                                Assert.assertEquals("mockKey", argMessage.getData().getKey());

                                P2PMessageCallback<GetDataResponse> callback =
                                        invocation.getArgument(2);
                                P2PMessage<GetDataResponse> response =
                                        new P2PMessage<GetDataResponse>();
                                response.setSeq(argMessage.getSeq());
                                response.setMethod(argMessage.getMethod());
                                response.setVersion(argMessage.getVersion());
                                response.setData(new GetDataResponse());
                                response.getData().setErrorCode(0);
                                response.getData().setErrorMessage("getData test resource success");
                                response.getData().setValue(argMessage.getData().toString());

                                callback.onResponse(0, "getData test resource success", response);

                                return null;
                            }
                        })
                .when(p2pMessageEngine)
                .asyncSendMessage(Mockito.any(), Mockito.any(), Mockito.any());

        com.webank.wecross.resource.Resource resource =
                new RemoteResource(peer, 1, p2pMessageEngine);
        resource.setPath(Path.decode("test-network.test-stub.test-local-resource"));

        GetDataRequest request = new GetDataRequest();
        request.setKey("mockKey");

        GetDataResponse response = resource.getData(request);
        Assert.assertEquals(Integer.valueOf(0), response.getErrorCode());
        Assert.assertTrue(response.getErrorMessage().contains("getData test resource success"));
        Assert.assertEquals(request.toString(), response.getValue());
        System.out.println(response.getErrorMessage());
    }

    @Test
    public void remoteResourceSetDataTest() throws Exception {
        Peer peer = new Peer(new Node("", "", 0));
        P2PMessageEngine p2pMessageEngine = Mockito.spy(RestfulP2PMessageEngine.class);
        Mockito.doAnswer(
                        new Answer<Object>() {
                            @Override
                            public Object answer(InvocationOnMock invocation) throws Throwable {
                                Peer argPeer = invocation.getArgument(0);
                                Assert.assertEquals(peer, argPeer);

                                P2PMessage<SetDataRequest> argMessage = invocation.getArgument(1);
                                Assert.assertEquals(
                                        "test-network/test-stub/test-local-resource/setData",
                                        argMessage.getMethod());
                                Assert.assertEquals("mockKey", argMessage.getData().getKey());
                                Assert.assertEquals("mockValue", argMessage.getData().getValue());

                                P2PMessageCallback<SetDataResponse> callback =
                                        invocation.getArgument(2);
                                P2PMessage<SetDataResponse> response =
                                        new P2PMessage<SetDataResponse>();
                                response.setSeq(argMessage.getSeq());
                                response.setMethod(argMessage.getMethod());
                                response.setVersion(argMessage.getVersion());
                                response.setData(new SetDataResponse());
                                response.getData().setErrorCode(0);
                                response.getData().setErrorMessage("setData test resource success");

                                callback.onResponse(
                                        0, "sendTransaction test resource success", response);

                                return null;
                            }
                        })
                .when(p2pMessageEngine)
                .asyncSendMessage(Mockito.any(), Mockito.any(), Mockito.any());

        com.webank.wecross.resource.Resource resource =
                new RemoteResource(peer, 1, p2pMessageEngine);
        resource.setPath(Path.decode("test-network.test-stub.test-local-resource"));

        SetDataRequest request = new SetDataRequest();
        request.setKey("mockKey");
        request.setValue("mockValue");

        SetDataResponse response = resource.setData(request);
        Assert.assertEquals(Integer.valueOf(0), response.getErrorCode());
        Assert.assertTrue(response.getErrorMessage().contains("setData test resource success"));
        System.out.println(response.getErrorMessage());
    }

    @Test
    public void remoteResourceCallTest() throws Exception {
        Peer peer = new Peer(new Node("", "", 0));

        P2PMessageEngine p2pMessageEngine = Mockito.spy(RestfulP2PMessageEngine.class);
        Mockito.doAnswer(
                        new Answer<Object>() {
                            @Override
                            public Object answer(InvocationOnMock invocation) throws Throwable {
                                Peer argPeer = invocation.getArgument(0);
                                Assert.assertEquals(peer, argPeer);

                                P2PMessage<TransactionRequest> argMessage =
                                        invocation.getArgument(1);
                                Assert.assertEquals(
                                        "test-network/test-stub/test-local-resource/call",
                                        argMessage.getMethod());
                                Assert.assertArrayEquals(
                                        new String[] {"123", "aacbb"},
                                        argMessage.getData().getArgs());

                                P2PMessageCallback<TransactionResponse> callback =
                                        invocation.getArgument(2);
                                P2PMessage<TransactionResponse> response =
                                        new P2PMessage<TransactionResponse>();
                                response.setSeq(argMessage.getSeq());
                                response.setMethod(argMessage.getMethod());
                                response.setVersion(argMessage.getVersion());
                                response.setData(new TransactionResponse());
                                response.getData().setErrorCode(0);
                                response.getData().setErrorMessage("call test resource success");
                                response.getData().setResult(new Object[] {argMessage.getData()});

                                callback.onResponse(
                                        0, "sendTransaction test resource success", response);

                                return null;
                            }
                        })
                .when(p2pMessageEngine)
                .asyncSendMessage(Mockito.any(), Mockito.any(), Mockito.any());

        com.webank.wecross.resource.Resource resource =
                new RemoteResource(peer, 1, p2pMessageEngine);
        resource.setPath(Path.decode("test-network.test-stub.test-local-resource"));

        TransactionRequest request = new TransactionRequest();
        request.setMethod("get");
        request.setArgs(new String[] {"123", "aacbb"});

        TransactionResponse response = resource.call(request);
        Assert.assertEquals(Integer.valueOf(0), response.getErrorCode());
        Assert.assertTrue(response.getErrorMessage().contains("call test resource success"));
        Assert.assertEquals(request, response.getResult()[0]);
        System.out.println(response.getErrorMessage());
    }

    @Test
    public void remoteResourceSendTransactionTest() throws Exception {
        Peer peer = new Peer(new Node("", "", 0));

        P2PMessageEngine p2pMessageEngine = Mockito.spy(RestfulP2PMessageEngine.class);
        Mockito.doAnswer(
                        new Answer<Object>() {
                            @Override
                            public Object answer(InvocationOnMock invocation) throws Throwable {
                                Peer argPeer = invocation.getArgument(0);
                                Assert.assertEquals(peer, argPeer);

                                P2PMessage<TransactionRequest> argMessage =
                                        invocation.getArgument(1);
                                Assert.assertEquals(
                                        "test-network/test-stub/test-local-resource/sendTransaction",
                                        argMessage.getMethod());
                                Assert.assertArrayEquals(
                                        new String[] {"123", "aabb"},
                                        argMessage.getData().getArgs());

                                P2PMessageCallback<TransactionResponse> callback =
                                        invocation.getArgument(2);
                                P2PMessage<TransactionResponse> response =
                                        new P2PMessage<TransactionResponse>();
                                response.setSeq(argMessage.getSeq());
                                response.setMethod(argMessage.getMethod());
                                response.setVersion(argMessage.getVersion());
                                response.setData(new TransactionResponse());
                                response.getData().setErrorCode(0);
                                response.getData()
                                        .setErrorMessage("sendTransaction test resource success");
                                response.getData().setResult(new Object[] {argMessage.getData()});

                                callback.onResponse(
                                        0, "sendTransaction test resource success", response);

                                return null;
                            }
                        })
                .when(p2pMessageEngine)
                .asyncSendMessage(Mockito.any(), Mockito.any(), Mockito.any());

        com.webank.wecross.resource.Resource resource =
                new RemoteResource(peer, 1, p2pMessageEngine);
        resource.setPath(Path.decode("test-network.test-stub.test-local-resource"));

        TransactionRequest request = new TransactionRequest();
        request.setMethod("sendTransaction");
        request.setArgs(new String[] {"123", "aabb"});

        TransactionResponse response = resource.sendTransaction(request);
        Assert.assertEquals(Integer.valueOf(0), response.getErrorCode());
        Assert.assertTrue(
                response.getErrorMessage().contains("sendTransaction test resource success"));
        Assert.assertEquals(request, response.getResult()[0]);
        System.out.println(response.getErrorMessage());
    }
}
*/