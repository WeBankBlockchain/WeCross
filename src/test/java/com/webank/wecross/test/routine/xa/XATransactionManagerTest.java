package com.webank.wecross.test.routine.xa;

import com.webank.wecross.resource.Resource;
import com.webank.wecross.routine.xa.XATransactionManager;
import com.webank.wecross.routine.xa.XATransactionManager.Callback;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.BlockHeaderManager;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionException;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import com.webank.wecross.zone.Chain;
import com.webank.wecross.zone.Zone;
import com.webank.wecross.zone.ZoneManager;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class XATransactionManagerTest {
    @Test
    public void testPrepare() throws Exception {
        BlockHeaderManager blockHeaderManager = Mockito.mock(BlockHeaderManager.class);

        Chain chain = Mockito.mock(Chain.class);
        Mockito.when(chain.getBlockHeaderManager()).thenReturn(blockHeaderManager);

        Zone zone = Mockito.mock(Zone.class);
        Mockito.when(zone.getChain(Mockito.any(Path.class))).thenReturn(chain);

        Account account = Mockito.mock(Account.class);

        Resource proxyResource = Mockito.mock(Resource.class);
        proxyResource.setResourceInfo(new ResourceInfo());
        Mockito.doAnswer(
                        new Answer<Object>() {
                            @Override
                            public Object answer(InvocationOnMock invocation) throws Throwable {
                                TransactionContext<TransactionRequest> context =
                                        invocation.getArgument(0);
                                Resource.Callback callback = invocation.getArgument(1);

                                Assert.assertEquals(context.getAccount(), account);
                                Assert.assertEquals(
                                        context.getBlockHeaderManager(), blockHeaderManager);

                                TransactionRequest request = context.getData();

                                Assert.assertEquals("startTransaction", request.getMethod());
                                Assert.assertEquals("0001", request.getArgs()[0]);

                                if (request.getArgs()[1].startsWith("a.b")) {
                                    Assert.assertEquals("a.b.c1", request.getArgs()[1]);
                                    Assert.assertEquals("a.b.c2", request.getArgs()[2]);
                                } else {
                                    Assert.assertEquals("a.c.c1", request.getArgs()[1]);
                                    Assert.assertEquals("a.c.c2", request.getArgs()[2]);
                                }

                                TransactionResponse transactionResponse = new TransactionResponse();
                                transactionResponse.setErrorCode(0);
                                transactionResponse.setErrorMessage("");
                                transactionResponse.setResult(new String[] {"0"});

                                callback.onTransactionResponse(null, transactionResponse);

                                return null;
                            }
                        })
                .when(proxyResource)
                .asyncSendTransaction(Mockito.any(), Mockito.any());

        ZoneManager zoneManager = Mockito.mock(ZoneManager.class);
        Mockito.when(zoneManager.getZone(Mockito.any(Path.class))).thenReturn(zone);
        Mockito.when(zoneManager.getResource(Mockito.any(Path.class))).thenReturn(proxyResource);

        XATransactionManager xaTransactionManager = new XATransactionManager();
        xaTransactionManager.setZoneManager(zoneManager);

        String transactionID = "0001";

        List<Path> resources = new ArrayList<Path>();
        resources.add(Path.decode("a.b.c1"));
        resources.add(Path.decode("a.b.c2"));
        resources.add(Path.decode("a.c.c1"));
        resources.add(Path.decode("a.c.c2"));

        xaTransactionManager.asyncPrepare(
                transactionID,
                account,
                resources,
                new Callback() {
                    @Override
                    public void onResponse(Exception e, int result) {
                        Assert.assertNull(e);
                        Assert.assertEquals(0, result);
                    }
                });

        Mockito.doAnswer(
                        new Answer<Object>() {
                            @Override
                            public Object answer(InvocationOnMock invocation) throws Throwable {
                                TransactionContext<TransactionRequest> context =
                                        invocation.getArgument(0);
                                Resource.Callback callback = invocation.getArgument(1);

                                callback.onTransactionResponse(
                                        new TransactionException(-1, ""), null);

                                return null;
                            }
                        })
                .when(proxyResource)
                .asyncSendTransaction(Mockito.any(), Mockito.any());

        xaTransactionManager.asyncPrepare(
                transactionID,
                account,
                resources,
                new Callback() {
                    @Override
                    public void onResponse(Exception e, int result) {
                        Assert.assertNotNull(e);
                        Assert.assertEquals(-1, result);
                    }
                });

        Mockito.doAnswer(
                        new Answer<Object>() {
                            @Override
                            public Object answer(InvocationOnMock invocation) throws Throwable {
                                TransactionContext<TransactionRequest> context =
                                        invocation.getArgument(0);
                                Resource.Callback callback = invocation.getArgument(1);

                                TransactionResponse transactionResponse = new TransactionResponse();
                                transactionResponse.setErrorCode(0);
                                transactionResponse.setErrorMessage("");
                                transactionResponse.setResult(new String[] {"100"});

                                callback.onTransactionResponse(null, transactionResponse);

                                return null;
                            }
                        })
                .when(proxyResource)
                .asyncSendTransaction(Mockito.any(), Mockito.any());

        xaTransactionManager.asyncPrepare(
                transactionID,
                account,
                resources,
                new Callback() {
                    @Override
                    public void onResponse(Exception e, int result) {
                        Assert.assertNull(e);
                        Assert.assertEquals(100, result);
                    }
                });
    }
}
