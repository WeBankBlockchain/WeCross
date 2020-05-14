package com.webank.wecross.test.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.webank.wecross.network.p2p.netty.common.Node;
import com.webank.wecross.peer.Peer;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.Response;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

public class ResourceTest {

    @Test
    public void connectionTest() {
        Resource resource = new Resource();
        assertTrue(resource.isConnectionEmpty());

        Peer peer0 = new Peer(new Node("", "", 0));
        Connection connection = Mockito.mock(Connection.class);
        resource.addConnection(peer0, connection);

        assertFalse(resource.isConnectionEmpty());

        resource.removeConnection(peer0);
        assertTrue(resource.isConnectionEmpty());
    }

    @Test
    public void callAndTransactionTest() {
        Resource resource = new Resource();
        ResourceInfo resourceInfo = new ResourceInfo();
        Peer peer0 = new Peer(new Node("", "", 0));
        Connection connection0 = Mockito.mock(Connection.class);
        resource.addConnection(peer0, connection0);

        TransactionRequest request = new TransactionRequest();
        request.setArgs(new String[] {"Hello world!"});

        TransactionResponse response = new TransactionResponse();
        response.setErrorCode(0);

        Driver driver = Mockito.mock(Driver.class);
        Mockito.when(driver.call(Mockito.any(), Mockito.any())).thenReturn(response);
        Mockito.when(driver.sendTransaction(Mockito.any(), Mockito.any())).thenReturn(response);
        resource.setDriver(driver);

        TransactionResponse r =
                resource.call(
                        new TransactionContext<TransactionRequest>(
                                request, null, resourceInfo, null));
        assertEquals(response, r);

        r =
                resource.sendTransaction(
                        new TransactionContext<TransactionRequest>(
                                request, null, resourceInfo, null));
        assertEquals(response, r);
    }

    @Test
    public void asyncCallAndTransactionTest() throws Exception {
        Resource resource = new Resource();
        ResourceInfo resourceInfo = new ResourceInfo();
        Peer peer0 = new Peer(new Node("", "", 0));
        Connection connection0 = Mockito.mock(Connection.class);
        resource.addConnection(peer0, connection0);

        TransactionRequest request = new TransactionRequest();
        request.setArgs(new String[] {"Hello world!"});

        TransactionResponse response = new TransactionResponse();
        response.setErrorCode(0);

        Driver driver = Mockito.mock(Driver.class);
        Mockito.doAnswer(
                        (Answer<Void>)
                                invocation -> {
                                    Driver.Callback callback = invocation.getArgument(2);
                                    callback.onTransactionResponse(response);
                                    return null;
                                })
                .when(driver)
                .asyncCall(Mockito.any(), Mockito.any(), Mockito.any(Driver.Callback.class));

        Mockito.doAnswer(
                        (Answer<Void>)
                                invocation -> {
                                    Driver.Callback callback = invocation.getArgument(2);
                                    callback.onTransactionResponse(response);
                                    return null;
                                })
                .when(driver)
                .asyncSendTransaction(
                        Mockito.any(), Mockito.any(), Mockito.any(Driver.Callback.class));
        resource.setDriver(driver);

        resource.asyncCall(
                new TransactionContext<TransactionRequest>(request, null, resourceInfo, null),
                new Resource.Callback() {
                    @Override
                    public void onTransactionResponse(TransactionResponse transactionResponse) {
                        assertEquals(response, transactionResponse);

                        resource.asyncSendTransaction(
                                new TransactionContext<TransactionRequest>(
                                        request, null, resourceInfo, null),
                                new Resource.Callback() {
                                    @Override
                                    public void onTransactionResponse(
                                            TransactionResponse transactionResponse) {
                                        assertEquals(response, transactionResponse);
                                        System.out.println("Callback happend");
                                    }
                                });
                    }
                });

        Thread.sleep(1000);
    }

    @Test
    public void onRemoteTransactionTest() {
        Resource resource = new Resource();

        Peer peer0 = new Peer(new Node("", "", 0));
        Connection connection0 = Mockito.mock(Connection.class);

        Peer peer1 = new Peer(new Node("", "", 1));
        Connection connection1 = Mockito.mock(Connection.class);

        resource.addConnection(peer0, connection0);
        resource.addConnection(peer1, connection1);

        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setArgs(new String[] {"Hello world!"});

        Driver driver = Mockito.mock(Driver.class);
        Mockito.when(driver.decodeTransactionRequest("Helloworld".getBytes()))
                .thenReturn(
                        new TransactionContext<TransactionRequest>(
                                transactionRequest, null, null, null));
        resource.setDriver(driver);

        Request request = new Request();
        request.setData("Helloworld".getBytes());

        Response response = new Response();

        Mockito.doAnswer(
                (Answer<Void>)
                        invocation -> {
                            Connection.Callback callback = invocation.getArgument(1);
                            callback.onResponse(response);
                            return null;
                        })
                .when(connection0)
                .asyncSend(
                        Mockito.any(), Mockito.any(Connection.Callback.class));

        Mockito.doAnswer(
                (Answer<Void>)
                        invocation -> {
                            Connection.Callback callback = invocation.getArgument(1);
                            callback.onResponse(response);
                            return null;
                        })
                .when(connection1)
                .asyncSend(
                        Mockito.any(), Mockito.any(Connection.Callback.class));

        assertEquals(response, resource.onRemoteTransaction(request));
    }
}
