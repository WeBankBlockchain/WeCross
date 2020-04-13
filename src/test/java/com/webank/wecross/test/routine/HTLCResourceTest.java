package com.webank.wecross.test.routine;

import com.webank.wecross.resource.Resource;
import com.webank.wecross.routine.RoutineDefault;
import com.webank.wecross.routine.htlc.HTLCErrorCode;
import com.webank.wecross.routine.htlc.HTLCResource;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import com.webank.wecross.stub.VerifiedTransaction;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class HTLCResourceTest {
    @Test
    public void sendTransaction() {
        TransactionRequest transactionRequest =
                new TransactionRequest("unlock", new String[] {"h", "s", "tx", "100"});
        TransactionContext<TransactionRequest> request =
                new TransactionContext<TransactionRequest>(transactionRequest, null, null, null);
        Resource mockResource = Mockito.mock(Resource.class);
        HTLCResource htlcResource = new HTLCResource(true, mockResource, mockResource, "0x");
        htlcResource.setCounterpartyAddress("0x");
        Driver mockDriver = Mockito.mock(Driver.class);
        Mockito.when(mockResource.getDriver()).thenReturn(mockDriver);
        mockResource.setDriver(mockDriver);
        TransactionResponse transactionResponse = new TransactionResponse();
        transactionResponse.setResult(new String[] {RoutineDefault.SUCCESS_FLAG});
        Mockito.when(mockResource.sendTransaction(request)).thenReturn(new TransactionResponse());

        VerifiedTransaction verifiedTransaction =
                new VerifiedTransaction(
                        100,
                        "tx",
                        "0x",
                        new TransactionRequest("lock", new String[] {"h"}),
                        transactionResponse);
        Mockito.when(mockDriver.getVerifiedTransaction("tx", 100, null, null))
                .thenReturn(verifiedTransaction);
        TransactionResponse response = htlcResource.sendTransaction(request);
        Assert.assertNotNull(response);

        VerifiedTransaction unVerifiedTransaction =
                new VerifiedTransaction(
                        100,
                        "tx",
                        "0xx",
                        new TransactionRequest("lock", new String[] {"h"}),
                        transactionResponse);
        Mockito.when(mockDriver.getVerifiedTransaction("tx", 100, null, null))
                .thenReturn(unVerifiedTransaction);
        response = htlcResource.sendTransaction(request);
        Assert.assertEquals(
                response.getErrorCode().intValue(), HTLCErrorCode.ASSET_HTLC_VERIFY_ERROR);
    }

    //    @Test
    //    public void onRemoteTransaction() {
    //        Resource mockResource = Mockito.mock(Resource.class);
    //        HTLCResource resource = new HTLCResource(true, mockResource, mockResource, "0x");
    //        resource.setCounterpartyAddress("0x");
    //        Driver mockDriver = Mockito.mock(Driver.class);
    //        Request mockRequest = Mockito.mock(Request.class);
    //        Mockito.when(mockResource.getDriver()).thenReturn(mockDriver);
    //        Mockito.when(mockDriver.isTransaction(mockRequest)).thenReturn(true);
    //        Mockito.when(mockRequest.getData()).thenReturn(null);
    //
    //        TransactionRequest transactionRequest = new TransactionRequest("getSecret", null);
    //        TransactionContext<TransactionRequest> transactionContext =
    //                new TransactionContext<TransactionRequest>(transactionRequest, null, null,
    // null);
    //
    // Mockito.when(mockDriver.decodeTransactionRequest(null)).thenReturn(transactionContext);
    //        Response response = resource.onRemoteTransaction(mockRequest);
    //        Assert.assertEquals(response.getErrorCode(), HTLCErrorCode.ASSET_HTLC_NO_PERMISSION);
    //
    //        TransactionRequest newTransactionRequest =
    //                new TransactionRequest("unlock", new String[] {"h", "s", "tx", "100"});
    //        TransactionContext<TransactionRequest> newRequest =
    //                new TransactionContext<TransactionRequest>(newTransactionRequest, null, null,
    // null);
    //        Mockito.when(mockDriver.decodeTransactionRequest(null)).thenReturn(newRequest);
    //        TransactionResponse transactionResponse = new TransactionResponse();
    //        transactionResponse.setResult(new String[] {RoutineDefault.SUCCESS_FLAG});
    //        VerifiedTransaction verifiedTransaction =
    //                new VerifiedTransaction(
    //                        100,
    //                        "tx",
    //                        "0xx",
    //                        new TransactionRequest("lock", new String[] {"h"}),
    //                        transactionResponse);
    //        Mockito.when(mockDriver.getVerifiedTransaction("tx", 100, null, null))
    //                .thenReturn(verifiedTransaction);
    //        Response newResponse = resource.onRemoteTransaction(mockRequest);
    //        Assert.assertEquals(newResponse.getErrorCode(),
    // HTLCErrorCode.ASSET_HTLC_VERIFY_ERROR);
    //    }
}
