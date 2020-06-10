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
import java.util.HashSet;
import java.util.Set;
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

                                Set<String> paths = new HashSet<String>();
                                for (String path : request.getArgs()) {
                                    paths.add(path);
                                }
                                Assert.assertEquals(request.getArgs().length, paths.size());
                                if (request.getArgs()[1].startsWith("a.b")) {
                                    Assert.assertTrue(paths.contains("a.b.c1"));
                                    Assert.assertTrue(paths.contains("a.b.c2"));
                                } else {
                                    Assert.assertTrue(paths.contains("a.c.c1"));
                                    Assert.assertTrue(paths.contains("a.c.c2"));
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
        Mockito.when(zoneManager.getResource(Path.decode("a.b.WeCrossProxy")))
                .thenReturn(proxyResource);
        Mockito.when(zoneManager.getResource(Path.decode("a.c.WeCrossProxy")))
                .thenReturn(proxyResource);

        XATransactionManager xaTransactionManager = new XATransactionManager();
        xaTransactionManager.setZoneManager(zoneManager);

        String transactionID = "0001";

        Set<Path> resources = new HashSet<Path>();
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

    @Test
    public void testCommit() throws Exception {
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

                                Assert.assertEquals("commitTransaction", request.getMethod());
                                Assert.assertEquals("0001", request.getArgs()[0]);

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
        Mockito.when(zoneManager.getResource(Path.decode("a.b.WeCrossProxy")))
                .thenReturn(proxyResource);
        Mockito.when(zoneManager.getResource(Path.decode("a.c.WeCrossProxy")))
                .thenReturn(proxyResource);

        XATransactionManager xaTransactionManager = new XATransactionManager();
        xaTransactionManager.setZoneManager(zoneManager);

        String transactionID = "0001";

        Set<Path> resources = new HashSet<Path>();
        resources.add(Path.decode("a.b.c1"));
        resources.add(Path.decode("a.b.c2"));
        resources.add(Path.decode("a.c.c1"));
        resources.add(Path.decode("a.c.c2"));

        xaTransactionManager.asyncCommit(
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

        xaTransactionManager.asyncCommit(
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

        xaTransactionManager.asyncCommit(
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

    @Test
    public void testRollback() throws Exception {
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

                                Assert.assertEquals("rollbackTransaction", request.getMethod());
                                Assert.assertEquals("0001", request.getArgs()[0]);

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
        Mockito.when(zoneManager.getResource(Path.decode("a.b.WeCrossProxy")))
                .thenReturn(proxyResource);
        Mockito.when(zoneManager.getResource(Path.decode("a.c.WeCrossProxy")))
                .thenReturn(proxyResource);

        XATransactionManager xaTransactionManager = new XATransactionManager();
        xaTransactionManager.setZoneManager(zoneManager);

        String transactionID = "0001";

        Set<Path> resources = new HashSet<Path>();
        resources.add(Path.decode("a.b.c1"));
        resources.add(Path.decode("a.b.c2"));
        resources.add(Path.decode("a.c.c1"));
        resources.add(Path.decode("a.c.c2"));

        xaTransactionManager.asyncRollback(
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

        xaTransactionManager.asyncRollback(
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

        xaTransactionManager.asyncRollback(
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

    @Test
    public void testGetTransactionInfo() throws Exception {
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

                                Assert.assertEquals("getTransactionInfo", request.getMethod());
                                Assert.assertEquals("0001", request.getArgs()[0]);

                                TransactionResponse transactionResponse = new TransactionResponse();
                                transactionResponse.setErrorCode(0);
                                transactionResponse.setErrorMessage("");
                                transactionResponse.setResult(
                                        new String[] {
                                            ""
                                                    + "{\n"
                                                    + "    	\"transactionID\": \"1\",\n"
                                                    + "    	\"status\": 1,\n"
                                                    + "    	\"startTimestamp\": \"123\",\n"
                                                    + "    	\"commitTimestamp\": \"456\",\n"
                                                    + "    	\"rollbackTimestamp\": \"789\",\n"
                                                    + "    	\"transactionSteps\": [{\n"
                                                    + "            	\"seq\": 0,\n"
                                                    + "    			\"contract\": \"0x12\",\n"
                                                    + "    			\"path\": \"a.b.c\",\n"
                                                    + "    			\"timestamp\": \"123\",\n"
                                                    + "    			\"func\": \"test1(string)\",\n"
                                                    + "    			\"args\": \"aaa\"\n"
                                                    + "    		},\n"
                                                    + "    		{\n"
                                                    + "    		    \"seq\": 1,\n"
                                                    + "    			\"contract\": \"0x12\",\n"
                                                    + "    			\"path\": \"a.b.c\",\n"
                                                    + "    			\"timestamp\": \"123\",\n"
                                                    + "    			\"func\": \"test2(string)\",\n"
                                                    + "    			\"args\": \"bbb\"\n"
                                                    + "    		}\n"
                                                    + "    	]\n"
                                                    + "    }"
                                        });

                                callback.onTransactionResponse(null, transactionResponse);

                                return null;
                            }
                        })
                .when(proxyResource)
                .asyncCall(Mockito.any(), Mockito.any());

        ZoneManager zoneManager = Mockito.mock(ZoneManager.class);
        Mockito.when(zoneManager.getZone(Mockito.any(Path.class))).thenReturn(zone);
        Mockito.when(zoneManager.getResource(Path.decode("a.b.WeCrossProxy")))
                .thenReturn(proxyResource);
        Mockito.when(zoneManager.getResource(Path.decode("a.c.WeCrossProxy")))
                .thenReturn(proxyResource);

        XATransactionManager xaTransactionManager = new XATransactionManager();
        xaTransactionManager.setZoneManager(zoneManager);

        String transactionID = "0001";

        xaTransactionManager.asyncGetTransactionInfo(
                transactionID,
                Path.decode("a.b.c1"),
                account,
                (e, transactionInfo) -> {
                    Assert.assertNull(e);
                    Assert.assertEquals("1", transactionInfo.getTransactionID());
                    // Assert.assertEquals(expected, actual);

                    // Assert.assertEquals(5, transactionInfo.getPaths());
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
                .asyncCall(Mockito.any(), Mockito.any());

        xaTransactionManager.asyncGetTransactionInfo(
                transactionID,
                Path.decode("a.b.c1"),
                account,
                (e, transactionInfo) -> {
                    Assert.assertNotNull(e);
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
                                transactionResponse.setResult(new String[] {"wrong json here"});

                                callback.onTransactionResponse(null, transactionResponse);

                                return null;
                            }
                        })
                .when(proxyResource)
                .asyncCall(Mockito.any(), Mockito.any());

        xaTransactionManager.asyncGetTransactionInfo(
                transactionID,
                Path.decode("a.b.c1"),
                account,
                (e, transactionInfo) -> {
                    Assert.assertNotNull(e);
                    Assert.assertEquals("Decode transactionInfo json error", e.getMessage());
                });
    }
}
