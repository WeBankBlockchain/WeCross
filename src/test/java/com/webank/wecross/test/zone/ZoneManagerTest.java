package com.webank.wecross.test.zone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.webank.wecross.config.ResourceThreadPoolConfig;
import com.webank.wecross.network.p2p.netty.common.Node;
import com.webank.wecross.peer.Peer;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.StubFactory;
import com.webank.wecross.stubmanager.MemoryBlockHeaderManagerFactory;
import com.webank.wecross.stubmanager.StubManager;
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

        /*
        // test for different peer
        ChainInfo resourceInfo2 = new ChainInfo();
        resourceInfo2.setName("contract0");
        resourceInfo2.setStubType("test");

        Peer peer2 = new Peer(new Node("bbb", "127.0.0.1", 100));

        Map<String, ResourceInfo> resources2 = new HashMap<String, ResourceInfo>();
        resources2.put("payment.bcos0.contract0", resourceInfo2);
        zoneManager.addRemoteChains(peer2, resources2);

        Map<String, ResourceInfo> allResources3 = zoneManager.getAllChainsInfo(false);
        System.out.println(allResources3);
        Assert.assertEquals(12, allResources3.size());
        */

        // Resource resource2 = zoneManager.getResource(Path.decode("payment.bcos0.contract0"));
        // Assert.assertEquals(2, resource2.getPeers().size());
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

        Map<String, ChainInfo> allResources = zoneManager.getAllChainsInfo(false);
        Assert.assertEquals(3, allResources.size());

        ChainInfo removeChain = new ChainInfo();
        removeChain.setName("bcos0");
        removeChain.setResources(new ArrayList<ResourceInfo>());

        Map<String, ChainInfo> removeResources = new HashMap<String, ChainInfo>();
        removeResources.put("payment.bcos0", removeChain);

        for (int i = 0; i < 4; ++i) {
            ResourceInfo resourceInfo = new ResourceInfo();
            resourceInfo.setName("chain" + i);
            resourceInfo.setStubType("test");

            removeChain.getResources().add(resourceInfo);
        }

        zoneManager.removeRemoteChains(peer, removeResources, false);

        allResources = zoneManager.getAllChainsInfo(false);
        Assert.assertEquals(3, allResources.size()); // all resources removed, 1 connection left

        removeChain.getResources().clear();
        zoneManager.removeRemoteChains(peer, removeResources, true);
        allResources = zoneManager.getAllChainsInfo(false);
        Assert.assertEquals(2, allResources.size()); // connection removed, 1 connection left

        ChainInfo resourceInfo2 = new ChainInfo();
        resourceInfo2.setName("bcos1");
        resourceInfo2.setStubType("test");

        Peer peer2 = new Peer(new Node("bbb", "127.0.0.1", 100));

        Map<String, ChainInfo> chains2 = new HashMap<String, ChainInfo>();
        chains2.put("payment.bcos1", resourceInfo2);
        zoneManager.addRemoteChains(peer2, chains2);

        // removeResource.setName("payment.bcos1.contract1");
        removeResources.clear();
        removeResources.put("payment.bcos1", removeChain);
        zoneManager.removeRemoteChains(peer2, removeResources, true);

        allResources = zoneManager.getAllChainsInfo(false);
        Assert.assertEquals(2, allResources.size()); // all 2 connection remove 1

        /*
        Zone zone = zoneManager.getZone("payment");
        Chain stub = zone.getChain("bcos2");
        Assert.assertNull(stub);

        // fatal test
        ChainInfo resourceInfo = new ChainInfo();
        resourceInfo.setName("payment.bcos2.aaa");

        Map<String, ChainInfo> resources = new HashMap<String, ChainInfo>();
        resources.put("payment.bcos2.aaa", resourceInfo);
        zoneManager.removeRemoteChains(peer2, resources);

        resources.clear();
        resources.put("payment.bcos1.contract0", resourceInfo);
        // resourceInfo.setName("payment.bcos1.contract0");
        zoneManager.removeRemoteChains(peer2, resources);

        resources.clear();
        resources.put("payment.bcos1.abc", resourceInfo);
        // resourceInfo.setName("payment.bcos1.abc");
        zoneManager.removeRemoteChains(peer2, resources);

        resources.clear();
        resources.put("payment1.bcos4.abc", resourceInfo);
        // resourceInfo.setName("payment1.bcos4.abc");
        zoneManager.removeRemoteChains(peer2, resources);

        resources.clear();
        resources.put("payment.abc", resourceInfo);
        // resourceInfo.setName("payment.abc");
        zoneManager.removeRemoteChains(peer2, resources);

        for (Map.Entry<String, ChainInfo> entry :
                zoneManager.getAllChainsInfo(false).entrySet()) {
            resources = new HashMap<String, ChainInfo>();
            resources.put(entry.getKey(), entry.getValue());
            zoneManager.removeRemoteChains(peer, resources);
        }

        Assert.assertEquals(0, zoneManager.getZones().size());
        */
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
