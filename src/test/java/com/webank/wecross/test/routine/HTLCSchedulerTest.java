package com.webank.wecross.test.routine;

import com.webank.wecross.routine.htlc.HTLC;
import com.webank.wecross.routine.htlc.HTLCResource;
import com.webank.wecross.routine.htlc.HTLCResourcePair;
import com.webank.wecross.routine.htlc.HTLCScheduler;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import com.webank.wecross.stub.VerifiedTransaction;
import java.math.BigInteger;
import org.junit.Test;
import org.mockito.Mockito;

public class HTLCSchedulerTest {
    @Test
    public void startTest1() throws Exception {

        HTLC mockHTLC = Mockito.mock(HTLC.class);
        HTLCScheduler htlcScheduler = new HTLCScheduler(mockHTLC);

        String h = "test";
        HTLCResourcePair mockHTLCResourcePair = Mockito.mock(HTLCResourcePair.class);
        HTLCResource mockHTLCResource = Mockito.mock(HTLCResource.class);
        Mockito.when(mockHTLCResourcePair.getSelfHTLCResource()).thenReturn(mockHTLCResource);
        Mockito.when(mockHTLCResourcePair.getCounterpartyHTLCResource())
                .thenReturn(mockHTLCResource);

        // checkSelfRollback
        Mockito.when(mockHTLC.getSelfRollbackStatus(mockHTLCResource, h)).thenReturn(false);
        BigInteger now = BigInteger.valueOf(System.currentTimeMillis() / 1000);
        Mockito.when(mockHTLC.getSelfTimelock(mockHTLCResource, h))
                .thenReturn(now.add(BigInteger.valueOf(1000)));

        // checkSelfRollback
        Mockito.when(mockHTLC.getCounterpartyRollbackStatus(mockHTLCResource, h)).thenReturn(false);
        Mockito.when(mockHTLC.getCounterpartyTimelock(mockHTLCResource, h))
                .thenReturn(now.add(BigInteger.valueOf(1000)));

        // getSecret
        Mockito.when(mockHTLC.getSecret(mockHTLCResource, h)).thenReturn("test");
        Mockito.when(mockHTLCResource.getSelfPath()).thenReturn(new Path());

        // checkContractInfo
        Mockito.when(mockHTLCResource.getCounterpartyAddress()).thenReturn("0x");
        Mockito.when(mockHTLC.getNewContractTxInfo(mockHTLCResource, h))
                .thenReturn(new String[] {"0x", "100"});
        Driver mockDriver = Mockito.mock(Driver.class);
        Mockito.when(mockHTLCResource.getDriver()).thenReturn(mockDriver);
        Mockito.when(mockHTLCResource.getResourceBlockHeaderManager()).thenReturn(null);
        Mockito.when(mockHTLCResource.chooseConnection()).thenReturn(null);
        TransactionRequest request =
                new TransactionRequest("newContract", new String[] {"hello", "true"});
        TransactionResponse response = new TransactionResponse();
        response.setResult(new String[] {"hello", "world"});
        VerifiedTransaction verifiedTransaction =
                new VerifiedTransaction(100, "0x", "0x", request, response);
        Mockito.when(mockDriver.getVerifiedTransaction("0x", 100, null, null))
                .thenReturn(verifiedTransaction);

        // handleSelfLock
        Mockito.when(mockHTLC.getSelfLockStatus(mockHTLCResource, h)).thenReturn(false);
        TransactionResponse transactionResponse = new TransactionResponse();
        transactionResponse.setHash("0x");
        transactionResponse.setBlockNumber(100);
        Mockito.when(mockHTLC.lock(mockHTLCResource, h)).thenReturn(transactionResponse);

        htlcScheduler.start(mockHTLCResourcePair, h);
        System.out.println("kkk");
    }

    @Test
    public void startTest2() throws Exception {

        HTLC mockHTLC = Mockito.mock(HTLC.class);
        HTLCScheduler htlcScheduler = new HTLCScheduler(mockHTLC);

        String h = "test";
        HTLCResourcePair mockHTLCResourcePair = Mockito.mock(HTLCResourcePair.class);
        HTLCResource mockHTLCResource = Mockito.mock(HTLCResource.class);
        Mockito.when(mockHTLCResourcePair.getSelfHTLCResource()).thenReturn(mockHTLCResource);

        Mockito.when(mockHTLCResourcePair.getCounterpartyHTLCResource())
                .thenReturn(mockHTLCResource);

        // checkSelfRollback
        Mockito.when(mockHTLC.getSelfRollbackStatus(mockHTLCResource, h)).thenReturn(true);
        // checkSelfRollback
        Mockito.when(mockHTLC.getCounterpartyRollbackStatus(mockHTLCResource, h)).thenReturn(true);

        htlcScheduler.start(mockHTLCResourcePair, h);
        System.out.println("kkk");
    }
}
