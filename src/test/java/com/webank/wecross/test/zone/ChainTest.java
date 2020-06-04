package com.webank.wecross.test.zone;

import com.webank.wecross.stub.Driver;
import com.webank.wecross.zone.Chain;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class ChainTest {
    private long localBlockNumber;
    private long remoteBlockNumber;

    @Test
    public void testFetchBlockHeader() {
        localBlockNumber = (long) -1;
        remoteBlockNumber = (long) 100;

        Driver driver = Mockito.spy(Driver.class);
        Mockito.when(driver.getBlockNumber(Mockito.any())).thenReturn(remoteBlockNumber);
        Mockito.when(driver.getBlockHeader(Mockito.anyLong(), Mockito.any()))
                .thenAnswer(
                        new Answer<byte[]>() {
                            @Override
                            public byte[] answer(InvocationOnMock invocation) throws Throwable {
                                long blockNumber = invocation.getArgument(0);

                                Assert.assertEquals(localBlockNumber + 1, blockNumber);
                                Assert.assertTrue(blockNumber <= remoteBlockNumber);

                                String blockHeader = "Block:" + String.valueOf(blockNumber);

                                return blockHeader.getBytes();
                            }
                        });

        Chain chain = new Chain("MockChain");
        chain.setDriver(driver);
        chain.addConnection(null, null);
    }
}
