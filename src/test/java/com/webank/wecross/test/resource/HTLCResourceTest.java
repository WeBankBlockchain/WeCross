package com.webank.wecross.test.resource;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.routine.htlc.HTLCQueryStatus;
import com.webank.wecross.routine.htlc.HTLCResource;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.Response;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionRequest;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class HTLCResourceTest {

    @Test
    public void handleRequestTest() throws Exception {
        Resource resource = new Resource();
        ResourceInfo resourceInfo = new ResourceInfo();
        HTLCResource assetHtlcResource = new HTLCResource(resource);
        TransactionRequest request = new TransactionRequest();
        TransactionContext<TransactionRequest> context =
                new TransactionContext<TransactionRequest>(request, null, resourceInfo, null);

        try {
            request.setMethod("unlock");
            context.setData(request);
            assetHtlcResource.handleSendTransactionRequest(context);
        } catch (WeCrossException e) {
            Assert.assertEquals(
                    java.util.Optional.of(HTLCQueryStatus.ASSET_HTLC_REQUEST_ERROR),
                    java.util.Optional.of(e.getErrorCode()));
        }

        try {
            request.setMethod("unlock");
            request.setArgs(new String[] {"a", "b", "c"});
            context.setData(request);
            assetHtlcResource.handleSendTransactionRequest(context);
        } catch (WeCrossException e) {
            Assert.assertEquals(
                    java.util.Optional.of(HTLCQueryStatus.ASSET_HTLC_VERIFY_LOCK_ERROR),
                    java.util.Optional.of(e.getErrorCode()));
        }

        try {
            request.setMethod("lock");
            context.setData(request);
            TransactionContext<TransactionRequest> newContext =
                    assetHtlcResource.handleSendTransactionRequest(context);
            Assert.assertEquals(context, newContext);
        } catch (WeCrossException e) {
            Assert.fail();
        }
    }

    @Test
    public void onRemoteTransaction() {
        Resource originResource = Mockito.mock(Resource.class);
        HTLCResource resource = new HTLCResource(originResource);
        Driver mockDriver = Mockito.mock(Driver.class);
        Request mockRequest = Mockito.mock(Request.class);
        TransactionRequest request = new TransactionRequest("getSecret", null);
        TransactionContext<TransactionRequest> transactionContext =
                new TransactionContext<TransactionRequest>(request, null, null, null);
        Mockito.when(originResource.getDriver()).thenReturn(mockDriver);
        Mockito.when(mockDriver.isTransaction(mockRequest)).thenReturn(true);
        Mockito.when(mockRequest.getData()).thenReturn(null);
        Mockito.when(mockDriver.decodeTransactionRequest(null)).thenReturn(transactionContext);
        Response response = resource.onRemoteTransaction(mockRequest);
        Assert.assertEquals(response.getErrorCode(), HTLCQueryStatus.ASSET_HTLC_NO_PERMISSION);
    }
}
