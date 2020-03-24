package com.webank.wecross.test.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.webank.wecross.p2p.netty.common.Node;
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

        Mockito.when(connection0.send(request)).thenReturn(response);
        Mockito.when(connection1.send(request)).thenReturn(response);

        assertEquals(response, resource.onRemoteTransaction(request));
    }
}
