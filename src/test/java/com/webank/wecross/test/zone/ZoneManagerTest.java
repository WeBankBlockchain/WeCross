package com.webank.wecross.test.zone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.webank.wecross.config.ResourceThreadPoolConfig;
import com.webank.wecross.network.p2p.netty.common.Node;
import com.webank.wecross.peer.Peer;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.StubFactory;
import com.webank.wecross.stubmanager.MemoryBlockHeaderManagerFactory;
import com.webank.wecross.stubmanager.StubManager;
import com.webank.wecross.zone.Chain;
import com.webank.wecross.zone.ChainInfo;
import com.webank.wecross.zone.Zone;
import com.webank.wecross.zone.ZoneManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZoneManagerTest {
    private Logger logger = LoggerFactory.getLogger(ZoneManagerTest.class);

    @Test
    public void notExistsZone() throws Exception {
        ZoneManager zoneManager = new ZoneManager();
        assertNull(zoneManager.getResource(Path.decode("a.b.c")));
    }

    @Test
    public void setZones() {
        Map<String, Zone> networks = new HashMap<String, Zone>();

        ZoneManager zoneManager = new ZoneManager();
        zoneManager.setZones(networks);

        assertEquals(networks, zoneManager.getZones());
    }

    @Test
    public void addRemoteResourcesTest() throws Exception {
        StubFactory stubFactory = Mockito.spy(StubFactory.class);
        Mockito.when(stubFactory.newConnection(Mockito.anyString())).thenReturn(null);
        Mockito.when(stubFactory.newDriver()).thenReturn(null);

        StubManager stubManager = Mockito.mock(StubManager.class);
        Mockito.when(stubManager.getStubFactory(Mockito.anyString())).thenReturn(stubFactory);

        ResourceThreadPoolConfig.ResourceThreadPool resourceThreadPool =
                new ResourceThreadPoolConfig.ResourceThreadPool(10, 10, 200);
        MemoryBlockHeaderManagerFactory resourceBlockHeaderManagerFactory =
                new MemoryBlockHeaderManagerFactory(resourceThreadPool);

        ZoneManager zoneManager = new ZoneManager();
        zoneManager.setStubManager(stubManager);
        zoneManager.setResourceBlockHeaderManagerFactory(resourceBlockHeaderManagerFactory);

        assertEquals(stubManager, zoneManager.getStubManager());

        Peer peer = new Peer(new Node("aaa", "127.0.0.1", 100));

        for (int i = 0; i < 3; i++) {
            ChainInfo chainInfo = new ChainInfo();
            chainInfo.setName("chain" + i);
            chainInfo.setStubType("test");

            Map<String, String> properties = new HashMap<String, String>();
            properties.put("key", "value");
            chainInfo.setProperties(properties);
            chainInfo.setResources(new ArrayList<ResourceInfo>());

            for (int j = 0; j < 4; j++) {
                ResourceInfo resourceInfo = new ResourceInfo();
                resourceInfo.setName("chain" + j);
                resourceInfo.setStubType("test");

                chainInfo.getResources().add(resourceInfo);
            }

            String path = "payment.bcos" + i;
            Map<String, ChainInfo> chains = new HashMap<String, ChainInfo>();
            chains.put(path, chainInfo);

            zoneManager.addRemoteChains(peer, chains);
        }

        Map<String, ChainInfo> allChains = zoneManager.getAllChainsInfo(false);
        Assert.assertEquals(3, allChains.size());

        Assert.assertEquals(1, zoneManager.getSeq());

        Map<String, ChainInfo> allLocalChains = zoneManager.getAllChainsInfo(true);
        System.out.println(allLocalChains);
        Assert.assertEquals(0, allLocalChains.size());

        // test for wrong path
        ChainInfo chainInfo = new ChainInfo();
        chainInfo.setName("bcos");
        chainInfo.setStubType("test");

        Map<String, ChainInfo> resources = new HashMap<String, ChainInfo>();
        resources.put("payment", chainInfo);
        zoneManager.addRemoteChains(peer, resources);

        Map<String, ChainInfo> allResources2 = zoneManager.getAllChainsInfo(false);
        System.out.println(allResources2);
        Assert.assertEquals(3, allResources2.size());
    }

    @Test
    public void removeRemoteResourcesTest() throws Exception {
        StubFactory stubFactory = Mockito.spy(StubFactory.class);
        Mockito.when(stubFactory.newConnection(Mockito.anyString())).thenReturn(null);
        Mockito.when(stubFactory.newDriver()).thenReturn(null);

        StubManager stubManager = Mockito.mock(StubManager.class);
        Mockito.when(stubManager.getStubFactory("test")).thenReturn(stubFactory);

        ResourceThreadPoolConfig.ResourceThreadPool resourceThreadPool =
                new ResourceThreadPoolConfig.ResourceThreadPool(10, 10, 200);
        MemoryBlockHeaderManagerFactory resourceBlockHeaderManagerFactory =
                new MemoryBlockHeaderManagerFactory(resourceThreadPool);

        ZoneManager zoneManager = new ZoneManager();
        zoneManager.setStubManager(stubManager);
        zoneManager.setResourceBlockHeaderManagerFactory(resourceBlockHeaderManagerFactory);

        Peer peer = new Peer(new Node("aaa", "127.0.0.1", 100));

        for (int i = 0; i < 3; i++) {
            ChainInfo chainInfo = new ChainInfo();
            chainInfo.setName("chain" + i);
            chainInfo.setStubType("test");

            Map<String, String> properties = new HashMap<String, String>();
            properties.put("key", "value");
            chainInfo.setProperties(properties);
            chainInfo.setResources(new ArrayList<ResourceInfo>());

            for (int j = 0; j < 4; j++) {
                ResourceInfo resourceInfo = new ResourceInfo();
                resourceInfo.setName("chain" + j);
                resourceInfo.setStubType("test");

                chainInfo.getResources().add(resourceInfo);
            }

            String path = "payment.bcos" + i;
            Map<String, ChainInfo> chains = new HashMap<String, ChainInfo>();
            chains.put(path, chainInfo);

            zoneManager.addRemoteChains(peer, chains);
        }

        Map<String, ChainInfo> allChainsInfo = zoneManager.getAllChainsInfo(false);
        Assert.assertEquals(3, allChainsInfo.size());

        ChainInfo removeChain = new ChainInfo();
        removeChain.setName("bcos0");
        removeChain.setResources(new ArrayList<ResourceInfo>());

        Map<String, ChainInfo> removeChainInfos = new HashMap<String, ChainInfo>();
        removeChainInfos.put("payment.bcos0", removeChain);

        for (int i = 0; i < 4; ++i) {
            ResourceInfo resourceInfo = new ResourceInfo();
            resourceInfo.setName("chain" + i);
            resourceInfo.setStubType("test");

            removeChain.getResources().add(resourceInfo);
        }

        zoneManager.removeRemoteChains(peer, removeChainInfos, false);

        allChainsInfo = zoneManager.getAllChainsInfo(false);
        Assert.assertEquals(3, allChainsInfo.size()); // all resources removed, 1 connection left

        removeChain.getResources().clear();
        zoneManager.removeRemoteChains(peer, removeChainInfos, true);
        allChainsInfo = zoneManager.getAllChainsInfo(false);
        Assert.assertEquals(2, allChainsInfo.size()); // connection removed, 1 connection left

        ChainInfo resourceInfo2 = new ChainInfo();
        resourceInfo2.setName("bcos1");
        resourceInfo2.setStubType("test");

        Peer peer2 = new Peer(new Node("bbb", "127.0.0.1", 100));

        Map<String, ChainInfo> chains2 = new HashMap<String, ChainInfo>();
        chains2.put("payment.bcos1", resourceInfo2);
        zoneManager.addRemoteChains(peer2, chains2);

        // removeResource.setName("payment.bcos1.contract1");
        removeChainInfos.clear();
        removeChainInfos.put("payment.bcos1", removeChain);
        zoneManager.removeRemoteChains(peer2, removeChainInfos, true);

        allChainsInfo = zoneManager.getAllChainsInfo(false);
        Assert.assertEquals(2, allChainsInfo.size()); // all 2 connection remove 1
    }

    @Test
    public void testGetResource() throws Exception {
        StubFactory stubFactory = Mockito.spy(StubFactory.class);
        Mockito.when(stubFactory.newConnection(Mockito.anyString())).thenReturn(null);
        Mockito.when(stubFactory.newDriver()).thenReturn(null);

        StubManager stubManager = Mockito.mock(StubManager.class);
        Mockito.when(stubManager.getStubFactory("test")).thenReturn(stubFactory);

        ResourceThreadPoolConfig.ResourceThreadPool resourceThreadPool =
                new ResourceThreadPoolConfig.ResourceThreadPool(10, 10, 200);
        MemoryBlockHeaderManagerFactory resourceBlockHeaderManagerFactory =
                new MemoryBlockHeaderManagerFactory(resourceThreadPool);

        ZoneManager zoneManager = new ZoneManager();
        zoneManager.setStubManager(stubManager);
        zoneManager.setResourceBlockHeaderManagerFactory(resourceBlockHeaderManagerFactory);

        Peer peer = new Peer(new Node("aaa", "127.0.0.1", 100));

        for (int i = 0; i < 3; i++) {
            ChainInfo chainInfo = new ChainInfo();
            chainInfo.setName("chain" + i);
            chainInfo.setStubType("test");

            Map<String, String> properties = new HashMap<String, String>();
            properties.put("key", "value");
            chainInfo.setProperties(properties);
            chainInfo.setResources(new ArrayList<ResourceInfo>());

            for (int j = 0; j < 4; j++) {
                ResourceInfo resourceInfo = new ResourceInfo();
                resourceInfo.setName("resource" + j);
                resourceInfo.setStubType("test");

                chainInfo.getResources().add(resourceInfo);
            }

            String path = "payment.chain" + i;
            Map<String, ChainInfo> chains = new HashMap<String, ChainInfo>();
            chains.put(path, chainInfo);

            zoneManager.addRemoteChains(peer, chains);
        }

        Map<String, ChainInfo> allResources = zoneManager.getAllChainsInfo(false);
        Assert.assertEquals(3, allResources.size());

        Chain chain = zoneManager.getChain(Path.decode("payment.chain0"));

        Resource resource = zoneManager.getResource(Path.decode("payment.chain0.resource0"));
        Assert.assertFalse(resource.isTemporary());
        Assert.assertEquals(chain.getConnections().keySet(), resource.getConnections().keySet());

        resource = zoneManager.getResource(Path.decode("payment.chain0.resource4"));
        Assert.assertNull(resource);

        resource = zoneManager.fetchResource(Path.decode("payment.chain0.resource4"));
        Assert.assertTrue(resource.isTemporary());

        Assert.assertEquals(chain.getConnections().keySet(), resource.getConnections().keySet());

        Map<String, Resource> resources = zoneManager.getAllResources(false);
        Assert.assertEquals(12, resources.size());
    }

    @Test
    public void checksumTest() throws Exception {
        StubFactory stubFactory = Mockito.spy(StubFactory.class);
        Mockito.when(stubFactory.newConnection(Mockito.anyString())).thenReturn(null);
        Mockito.when(stubFactory.newDriver()).thenReturn(null);

        StubManager stubManager = Mockito.mock(StubManager.class);
        Mockito.when(stubManager.getStubFactory("test")).thenReturn(stubFactory);

        ResourceThreadPoolConfig.ResourceThreadPool resourceThreadPool =
                new ResourceThreadPoolConfig.ResourceThreadPool(10, 10, 200);
        MemoryBlockHeaderManagerFactory resourceBlockHeaderManagerFactory =
                new MemoryBlockHeaderManagerFactory(resourceThreadPool);

        ZoneManager zoneManager = new ZoneManager();
        zoneManager.setStubManager(stubManager);
        zoneManager.setResourceBlockHeaderManagerFactory(resourceBlockHeaderManagerFactory);

        // Add resource 1
        String path = "payment.bcos";
        ChainInfo chainInfo = new ChainInfo();
        chainInfo.setStubType("test");
        chainInfo.setName(path);
        Peer peer = new Peer(new Node("bbb", "127.0.0.1", 100));
        Map<String, ChainInfo> chains = new HashMap<String, ChainInfo>();
        chains.put(path, chainInfo);

        zoneManager.addRemoteChains(peer, chains);
        Assert.assertTrue(zoneManager.getChain(Path.decode(path)) != null);

        // Add same path diff checksum resource
        ChainInfo chainInfo2 = new ChainInfo();
        chainInfo2.setStubType("test");
        chainInfo2.setName(path);

        Map<String, ChainInfo> chains2 = new HashMap<String, ChainInfo>();
        chains2.put(path, chainInfo2);

        zoneManager.addRemoteChains(peer, chains2);
        Assert.assertTrue(zoneManager.getChain(Path.decode(path)) != null);
    }
}
