package com.webank.wecross.test.zone;

import com.webank.wecross.storage.BlockHeaderStorage;
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

        BlockHeaderStorage blockHeaderStorage = Mockito.spy(BlockHeaderStorage.class);
        Mockito.doAnswer(
                        new Answer<Object>() {
                            @Override
                            public Object answer(InvocationOnMock invocation) throws Throwable {
                                long blockNumber = invocation.getArgument(0);
                                byte[] blockHeader = invocation.getArgument(1);

                                Assert.assertTrue(blockNumber < remoteBlockNumber);
                                Assert.assertArrayEquals(
                                        blockHeader,
                                        ("Block:" + String.valueOf(blockNumber)).getBytes());

                                localBlockNumber = blockNumber;

                                return null;
                            }
                        })
                .when(blockHeaderStorage)
                .writeBlockHeader(Mockito.anyLong(), Mockito.any());

        Mockito.when(blockHeaderStorage.readBlockNumber()).thenReturn(localBlockNumber);

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
        chain.setBlockHeaderStorage(blockHeaderStorage);
        chain.setDriver(driver);
        chain.addConnection(null, null);
        chain.fetchBlockHeader();
    }
}
