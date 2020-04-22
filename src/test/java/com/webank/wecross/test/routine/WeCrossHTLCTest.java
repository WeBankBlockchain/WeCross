package com.webank.wecross.test.routine;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.routine.htlc.HTLCErrorCode;
import com.webank.wecross.routine.htlc.HTLCResource;
import com.webank.wecross.routine.htlc.WeCrossHTLC;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import com.webank.wecross.stub.VerifiedTransaction;
import java.math.BigInteger;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class WeCrossHTLCTest {

    @Test
    public void callTest() throws WeCrossException {
        HTLCResource mockResource = Mockito.mock(HTLCResource.class);
        TransactionResponse response = new TransactionResponse();
        response.setResult(new String[] {"success"});
        response.setErrorCode(0);
        Mockito.when(mockResource.getSelfPath()).thenReturn(new Path());
        Mockito.when(mockResource.call(Mockito.any(TransactionContext.class))).thenReturn(response);
        WeCrossHTLC weCrossHTLC = new WeCrossHTLC();
        String result = weCrossHTLC.call(mockResource, "test", new String[] {"h", "s"});
        Assert.assertEquals(result, "success");
    }

    @Test
    public void sendTransactionTest() throws WeCrossException {
        HTLCResource mockResource = Mockito.mock(HTLCResource.class);
        TransactionResponse response = new TransactionResponse();
        response.setResult(new String[] {"success"});
        response.setErrorCode(0);
        Mockito.when(mockResource.getSelfPath()).thenReturn(new Path());
        Mockito.when(mockResource.sendTransaction(Mockito.any(TransactionContext.class)))
                .thenReturn(response);
        WeCrossHTLC weCrossHTLC = new WeCrossHTLC();
        String result = weCrossHTLC.sendTransaction(mockResource, "test", new String[] {"h", "s"});
        Assert.assertEquals(result, "success");
    }

    @Test
    public void lockTest() throws WeCrossException {
        HTLCResource mockResource = Mockito.mock(HTLCResource.class);
        TransactionResponse response = new TransactionResponse();
        response.setErrorCode(0);
        response.setResult(new String[] {"success"});
        Mockito.when(mockResource.getSelfPath()).thenReturn(new Path());
        Mockito.when(mockResource.sendTransaction(Mockito.any(TransactionContext.class)))
                .thenReturn(response);
        WeCrossHTLC weCrossHTLC = new WeCrossHTLC();
        TransactionResponse result = weCrossHTLC.lock(mockResource, "h");
        Assert.assertEquals(result, response);
    }

    @Test
    public void lockWithVerifyTest() throws WeCrossException {
        HTLCResource mockResource = Mockito.mock(HTLCResource.class);
        TransactionResponse response = new TransactionResponse();
        response.setErrorCode(0);
        response.setResult(new String[] {"success"});
        response.setHash("tx");
        response.setBlockNumber(100);
        Mockito.when(mockResource.getSelfPath()).thenReturn(new Path());
        Mockito.when(mockResource.sendTransaction(Mockito.any(TransactionContext.class)))
                .thenReturn(response);

        mockResource.setCounterpartyAddress("0x");
        Driver mockDriver = Mockito.mock(Driver.class);
        Mockito.when(mockResource.getDriver()).thenReturn(mockDriver);
        mockResource.setDriver(mockDriver);

        VerifiedTransaction verifiedTransaction =
                new VerifiedTransaction(
                        100,
                        "tx",
                        "0x",
                        new TransactionRequest("lock", new String[] {"h"}),
                        response);
        Mockito.when(mockDriver.getVerifiedTransaction("tx", 100, null, null))
                .thenReturn(verifiedTransaction);

        WeCrossHTLC weCrossHTLC = new WeCrossHTLC();
        weCrossHTLC.lockWithVerify(mockResource, "0x", "h");
    }

    @Test
    public void unlockTest() throws WeCrossException {
        HTLCResource mockResource = Mockito.mock(HTLCResource.class);
        TransactionResponse response = new TransactionResponse();
        response.setErrorCode(0);
        Mockito.when(mockResource.getSelfPath()).thenReturn(new Path());
        Mockito.when(mockResource.sendTransaction(Mockito.any(TransactionContext.class)))
                .thenReturn(response);
        WeCrossHTLC weCrossHTLC = new WeCrossHTLC();
        TransactionResponse result = weCrossHTLC.unlock(mockResource, "0x", 100, "h", "s");
        Assert.assertEquals(result, response);
    }

    @Test
    public void unlockWithVerifyTest() throws WeCrossException {
        HTLCResource mockResource = Mockito.mock(HTLCResource.class);
        TransactionResponse response = new TransactionResponse();
        response.setErrorCode(0);
        response.setResult(new String[] {"success"});
        response.setHash("tx");
        response.setBlockNumber(100);
        Mockito.when(mockResource.getSelfPath()).thenReturn(new Path());
        Mockito.when(mockResource.sendTransaction(Mockito.any(TransactionContext.class)))
                .thenReturn(response);

        mockResource.setCounterpartyAddress("0x");
        Driver mockDriver = Mockito.mock(Driver.class);
        Mockito.when(mockResource.getDriver()).thenReturn(mockDriver);
        mockResource.setDriver(mockDriver);

        VerifiedTransaction verifiedTransaction =
                new VerifiedTransaction(
                        100,
                        "tx",
                        "0x",
                        new TransactionRequest("unlock", new String[] {"h", "s", "tx", "100"}),
                        response);
        Mockito.when(mockDriver.getVerifiedTransaction("tx", 100, null, null))
                .thenReturn(verifiedTransaction);

        WeCrossHTLC weCrossHTLC = new WeCrossHTLC();
        weCrossHTLC.unlockWithVerify(mockResource, "tx", 100, "0x", "h", "s");
    }

    @Test
    public void getCounterpartyHtlcTest() throws WeCrossException {
        Resource mockResource = Mockito.mock(Resource.class);
        TransactionResponse response = new TransactionResponse();
        response.setErrorCode(0);
        response.setResult(new String[] {"htlc"});
        Mockito.when(mockResource.call(Mockito.any(TransactionContext.class))).thenReturn(response);

        WeCrossHTLC weCrossHTLC = new WeCrossHTLC();
        String result =
                weCrossHTLC.getCounterpartyHtlc(
                        mockResource,
                        new Account() {
                            @Override
                            public String getName() {
                                return null;
                            }

                            @Override
                            public String getType() {
                                return null;
                            }

                            @Override
                            public String getIdentity() {
                                return null;
                            }
                        });
        Assert.assertEquals(result, "htlc");
    }

    @Test
    public void getNewContractTxInfoTest() {
        HTLCResource mockResource = Mockito.mock(HTLCResource.class);
        TransactionResponse response = new TransactionResponse();
        response.setResult(new String[] {"null"});
        response.setErrorCode(0);
        Mockito.when(mockResource.getSelfPath()).thenReturn(new Path());
        Mockito.when(mockResource.call(Mockito.any(TransactionContext.class))).thenReturn(response);
        WeCrossHTLC weCrossHTLC = new WeCrossHTLC();
        try {
            weCrossHTLC.getNewContractTxInfo(mockResource, "h");
        } catch (WeCrossException e) {
            Assert.assertEquals(
                    e.getInternalErrorCode().intValue(), HTLCErrorCode.GET_TX_INFO_ERROR);
        }
    }

    @Test
    public void getLockTxInfoTest() throws WeCrossException {
        HTLCResource mockResource = Mockito.mock(HTLCResource.class);
        TransactionResponse response = new TransactionResponse();
        response.setResult(new String[] {"0x 100"});
        response.setErrorCode(0);
        Mockito.when(mockResource.getSelfPath()).thenReturn(new Path());
        Mockito.when(mockResource.call(Mockito.any(TransactionContext.class))).thenReturn(response);
        WeCrossHTLC weCrossHTLC = new WeCrossHTLC();
        String[] result = weCrossHTLC.getLockTxInfo(mockResource, "h");
        Assert.assertEquals(result[0], "0x");
    }

    @Test
    public void htlcCallTest() throws WeCrossException {
        HTLCResource mockResource = Mockito.mock(HTLCResource.class);
        TransactionResponse response = new TransactionResponse();
        response.setResult(new String[] {"0"});
        response.setErrorCode(0);
        Mockito.when(mockResource.getSelfPath()).thenReturn(new Path());
        Mockito.when(mockResource.call(Mockito.any(TransactionContext.class))).thenReturn(response);
        WeCrossHTLC weCrossHTLC = new WeCrossHTLC();

        String result = weCrossHTLC.getSecret(mockResource, "h");
        Assert.assertEquals(result, "0");

        BigInteger bigInteger = weCrossHTLC.getSelfTimelock(mockResource, "h");
        Assert.assertEquals(bigInteger, BigInteger.valueOf(0));

        bigInteger = weCrossHTLC.getCounterpartyTimelock(mockResource, "h");
        Assert.assertEquals(bigInteger, BigInteger.valueOf(0));

        boolean status = weCrossHTLC.getSelfLockStatus(mockResource, "h");
        Assert.assertFalse(status);

        status = weCrossHTLC.getSelfUnlockStatus(mockResource, "h");
        Assert.assertFalse(status);

        status = weCrossHTLC.getSelfRollbackStatus(mockResource, "h");
        Assert.assertFalse(status);

        status = weCrossHTLC.getCounterpartyLockStatus(mockResource, "h");
        Assert.assertFalse(status);

        status = weCrossHTLC.getCounterpartyUnlockStatus(mockResource, "h");
        Assert.assertFalse(status);

        status = weCrossHTLC.getCounterpartyRollbackStatus(mockResource, "h");
        Assert.assertFalse(status);
    }

    @Test
    public void htlcSendTransactionTest() throws WeCrossException {
        HTLCResource mockResource = Mockito.mock(HTLCResource.class);
        TransactionResponse response = new TransactionResponse();
        response.setResult(new String[] {"success"});
        response.setErrorCode(0);
        Mockito.when(mockResource.getSelfPath()).thenReturn(new Path());
        Mockito.when(mockResource.sendTransaction(Mockito.any(TransactionContext.class)))
                .thenReturn(response);
        WeCrossHTLC weCrossHTLC = new WeCrossHTLC();
        String result = weCrossHTLC.rollback(mockResource, "h");
        Assert.assertEquals(result, "success");

        weCrossHTLC.deleteTask(mockResource, "h");
        weCrossHTLC.setLockTxInfo(mockResource, "h", "0x", 100);
        weCrossHTLC.setCounterpartyLockStatus(mockResource, "h");
        weCrossHTLC.setCounterpartyUnlockStatus(mockResource, "h");
        weCrossHTLC.setCounterpartyRollbackStatus(mockResource, "h");
    }
}
