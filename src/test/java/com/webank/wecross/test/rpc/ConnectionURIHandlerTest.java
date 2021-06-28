package com.webank.wecross.test.rpc;

import com.webank.wecross.account.AccountAccessControlFilter;
import com.webank.wecross.account.AccountManager;
import com.webank.wecross.account.DisabledAccountAccessControlFilter;
import com.webank.wecross.account.UniversalAccount;
import com.webank.wecross.network.rpc.handler.ConnectionURIHandler;
import com.webank.wecross.network.rpc.handler.ConnectionURIHandler.ChainDetail;
import com.webank.wecross.network.rpc.handler.ConnectionURIHandler.ListData;
import com.webank.wecross.network.rpc.handler.URIHandler.Callback;
import com.webank.wecross.restserver.RestResponse;
import com.webank.wecross.stub.BlockManager;
import com.webank.wecross.stub.BlockManager.GetBlockNumberCallback;
import com.webank.wecross.zone.Chain;
import com.webank.wecross.zone.ChainInfo;
import com.webank.wecross.zone.Zone;
import com.webank.wecross.zone.ZoneManager;
import io.netty.handler.codec.http.FullHttpResponse;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class ConnectionURIHandlerTest {
    @Test
    public void testListChains() throws Exception {
        ConnectionURIHandler connectionURIHandler = new ConnectionURIHandler();
        Map<String, Chain> chains = new HashMap<String, Chain>();

        BlockManager blockManager = Mockito.mock(BlockManager.class);
        Mockito.doAnswer(
                        new Answer<Object>() {
                            @Override
                            public Object answer(InvocationOnMock invocation) throws Throwable {
                                GetBlockNumberCallback callback = invocation.getArgument(0);
                                callback.onResponse(null, 100);
                                return null;
                            }
                        })
                .when(blockManager)
                .asyncGetBlockNumber(Mockito.any());

        for (int i = 0; i < 100; ++i) {
            String chainName = "test-chain" + String.valueOf(i);

            ChainInfo chainInfo = new ChainInfo();
            chainInfo.setZone("test-zone");
            chainInfo.setName(chainName);

            Chain chain = new Chain("test-chain" + String.valueOf(i), chainInfo, null, null);
            chain.setBlockManager(blockManager);
            chains.put(chainName, chain);
        }

        Zone zone = new Zone();
        zone.setChains(chains);
        ZoneManager zoneManager = Mockito.mock(ZoneManager.class);
        Mockito.when(zoneManager.getZone("test-zone")).thenReturn(zone);
        Mockito.when(zoneManager.getZone("test-zone2")).thenReturn(new Zone());
        connectionURIHandler.setZoneManager(zoneManager);

        AccountAccessControlFilter filter = new DisabledAccountAccessControlFilter(new String[0]);
        UniversalAccount mockAccount = Mockito.mock(UniversalAccount.class);
        Mockito.when(mockAccount.getAccessControlFilter()).thenReturn(filter);

        AccountManager accountManager = Mockito.mock(AccountManager.class);
        Mockito.when(accountManager.getUniversalAccount(null)).thenReturn(mockAccount);
        connectionURIHandler.setAccountManager(accountManager);

        AtomicInteger hit = new AtomicInteger(0);
        connectionURIHandler.handle(
                null,
                "/listChains",
                "GET",
                "",
                new Callback() {
                    @Override
                    public void onResponse(File restResponse) {
                        Assert.fail();
                    }

                    @Override
                    public void onResponse(RestResponse restResponse) {
                        ListData data = (ListData) restResponse.getData();
                        Assert.assertEquals(0, data.getSize());

                        List<ChainDetail> chainDetails = (List<ChainDetail>) data.getData();
                        Assert.assertEquals(0, chainDetails.size());

                        hit.addAndGet(1);
                    }

                    @Override
                    public void onResponse(String restResponse) {
                        Assert.fail();
                    }

                    @Override
                    public void onResponse(FullHttpResponse fullHttpResponse) {

                        Assert.fail();
                    }
                });

        Assert.assertEquals(1, hit.intValue());

        connectionURIHandler.handle(
                null,
                "/listChains?zone=test-zone&offset=1000&size=100",
                "GET",
                "",
                new Callback() {
                    @Override
                    public void onResponse(File restResponse) {
                        Assert.fail();
                    }

                    @Override
                    public void onResponse(RestResponse restResponse) {
                        ListData data = (ListData) restResponse.getData();
                        Assert.assertEquals(0, data.getSize());

                        List<ChainDetail> chainDetails = (List<ChainDetail>) data.getData();
                        Assert.assertEquals(0, chainDetails.size());

                        hit.addAndGet(1);
                    }

                    @Override
                    public void onResponse(String restResponse) {
                        Assert.fail();
                    }

                    @Override
                    public void onResponse(FullHttpResponse fullHttpResponse) {
                        Assert.fail();
                    }
                });

        Assert.assertEquals(2, hit.intValue());

        Set<String> paths = new HashSet<String>();
        connectionURIHandler.handle(
                null,
                "/listChains?zone=test-zone&offset=0&size=0",
                "GET",
                "",
                new Callback() {
                    @Override
                    public void onResponse(File restResponse) {
                        Assert.fail();
                    }

                    @Override
                    public void onResponse(RestResponse restResponse) {
                        ListData data = (ListData) restResponse.getData();
                        Assert.assertEquals(100, data.getSize());

                        List<ChainDetail> chainDetails = (List<ChainDetail>) data.getData();
                        Assert.assertEquals(100, chainDetails.size());

                        for (int j = 0; j < 100; ++j) {
                            Assert.assertFalse(paths.contains(chainDetails.get(j).getChain()));
                            paths.add(chainDetails.get(j).getChain());
                            Assert.assertEquals(100, chainDetails.get(j).getBlockNumber());
                        }

                        hit.addAndGet(1);
                    }

                    @Override
                    public void onResponse(String restResponse) {
                        Assert.fail();
                    }

                    @Override
                    public void onResponse(FullHttpResponse fullHttpResponse) {
                        Assert.fail();
                    }
                });

        Assert.assertEquals(3, hit.intValue());

        paths.clear();
        for (int i = 0; i < 10; ++i) {
            String queryString = "zone=test-zone&offset=" + String.valueOf(i * 10) + "&size=10";

            final int index = i;
            connectionURIHandler.handle(
                    null,
                    "/listChains?" + queryString,
                    "GET",
                    "",
                    new Callback() {
                        @Override
                        public void onResponse(File restResponse) {
                            Assert.fail();
                        }

                        @Override
                        public void onResponse(RestResponse restResponse) {
                            ListData data = (ListData) restResponse.getData();
                            Assert.assertEquals(100, data.getSize());

                            List<ChainDetail> chainDetails = (List<ChainDetail>) data.getData();
                            Assert.assertEquals(10, chainDetails.size());

                            for (int j = 0; j < 10; ++j) {
                                Assert.assertFalse(paths.contains(chainDetails.get(j).getChain()));
                                paths.add(chainDetails.get(j).getChain());
                                Assert.assertEquals(100, chainDetails.get(j).getBlockNumber());
                            }

                            hit.addAndGet(1);
                        }

                        @Override
                        public void onResponse(String restResponse) {
                            Assert.fail();
                        }

                        @Override
                        public void onResponse(FullHttpResponse fullHttpResponse) {
                            Assert.fail();
                        }
                    });
        }

        Assert.assertEquals(13, hit.intValue());

        connectionURIHandler.handle(
                null,
                "/listChains?zone=test-zone2",
                "GET",
                "",
                new Callback() {
                    @Override
                    public void onResponse(File restResponse) {
                        Assert.fail();
                    }

                    @Override
                    public void onResponse(RestResponse restResponse) {
                        ListData data = (ListData) restResponse.getData();
                        Assert.assertEquals(0, data.getSize());

                        hit.addAndGet(1);
                    }

                    @Override
                    public void onResponse(String restResponse) {
                        Assert.fail();
                    }

                    @Override
                    public void onResponse(FullHttpResponse fullHttpResponse) {
                        Assert.fail();
                    }
                });

        Assert.assertEquals(14, hit.intValue());

        connectionURIHandler.handle(
                null,
                "/listChains?zone=test-zone&offset=0&size=0",
                "GET",
                "",
                new Callback() {
                    @Override
                    public void onResponse(File restResponse) {
                        Assert.fail();
                    }

                    @Override
                    public void onResponse(RestResponse restResponse) {
                        ListData data = (ListData) restResponse.getData();
                        Assert.assertEquals(100, data.getSize());

                        hit.addAndGet(1);
                    }

                    @Override
                    public void onResponse(String restResponse) {
                        Assert.fail();
                    }

                    @Override
                    public void onResponse(FullHttpResponse fullHttpResponse) {
                        Assert.fail();
                    }
                });

        Assert.assertEquals(15, hit.intValue());

        paths.clear();
        connectionURIHandler.handle(
                null,
                "/listChains?zone=test-zone&offset=95&size=50",
                "GET",
                "",
                new Callback() {
                    @Override
                    public void onResponse(File restResponse) {
                        Assert.fail();
                    }

                    @Override
                    public void onResponse(RestResponse restResponse) {
                        ListData data = (ListData) restResponse.getData();
                        Assert.assertEquals(100, data.getSize());

                        List<ChainDetail> chainDetails = (List<ChainDetail>) data.getData();
                        Assert.assertEquals(5, chainDetails.size());

                        for (int j = 95; j < 100; ++j) {
                            Assert.assertFalse(paths.contains(chainDetails.get(j - 95).getChain()));
                            paths.add(chainDetails.get(j - 95).getChain());
                            Assert.assertEquals(100, chainDetails.get(j - 95).getBlockNumber());
                        }

                        hit.addAndGet(1);
                    }

                    @Override
                    public void onResponse(String restResponse) {
                        Assert.fail();
                    }

                    @Override
                    public void onResponse(FullHttpResponse fullHttpResponse) {
                        Assert.fail();
                    }
                });

        Assert.assertEquals(16, hit.intValue());
    }

    @Test
    public void testListZones() throws Exception {
        ConnectionURIHandler connectionURIHandler = new ConnectionURIHandler();

        Map<String, Zone> zones = new HashMap<String, Zone>();
        for (int i = 0; i < 100; ++i) {
            Zone zone = new Zone();
            zones.put("test-zone" + String.valueOf(i), zone);
        }

        ZoneManager zoneManager = Mockito.mock(ZoneManager.class);
        Mockito.when(zoneManager.getZones()).thenReturn(zones);

        connectionURIHandler.setZoneManager(zoneManager);

        Set<String> paths = new HashSet<String>();
        for (int i = 0; i < 10; ++i) {
            String queryString = "offset=" + String.valueOf(i * 10) + "&size=10";

            final int index = i;
            connectionURIHandler.handle(
                    null,
                    "/listZones?" + queryString,
                    "GET",
                    "",
                    new Callback() {
                        @Override
                        public void onResponse(File restResponse) {
                            Assert.fail();
                        }

                        @Override
                        public void onResponse(RestResponse restResponse) {
                            ListData data = (ListData) restResponse.getData();
                            Assert.assertEquals(100, data.getSize());

                            List<String> zoneDetails = (List<String>) data.getData();
                            Assert.assertEquals(10, zoneDetails.size());

                            for (int j = 0; j < 10; ++j) {
                                Assert.assertFalse(paths.contains(zoneDetails.get(j)));
                                paths.add(zoneDetails.get(j));
                            }
                        }

                        @Override
                        public void onResponse(String restResponse) {
                            Assert.fail();
                        }

                        @Override
                        public void onResponse(FullHttpResponse fullHttpResponse) {
                            Assert.fail();
                        }
                    });
        }

        paths.clear();
        connectionURIHandler.handle(
                null,
                "/listZones?offset=95&size=50",
                "GET",
                "",
                new Callback() {
                    @Override
                    public void onResponse(File restResponse) {
                        Assert.fail();
                    }

                    @Override
                    public void onResponse(RestResponse restResponse) {
                        ListData data = (ListData) restResponse.getData();
                        Assert.assertEquals(100, data.getSize());

                        List<String> zoneDetails = (List<String>) data.getData();
                        Assert.assertEquals(5, zoneDetails.size());

                        for (int j = 0; j < 5; ++j) {
                            Assert.assertFalse(paths.contains(zoneDetails.get(j)));
                            paths.add(zoneDetails.get(j));
                        }
                    }

                    @Override
                    public void onResponse(String restResponse) {
                        Assert.fail();
                    }

                    @Override
                    public void onResponse(FullHttpResponse fullHttpResponse) {
                        Assert.fail();
                    }
                });
    }
}
