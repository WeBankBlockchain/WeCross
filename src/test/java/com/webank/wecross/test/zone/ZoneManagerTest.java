package com.webank.wecross.test.zone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.webank.wecross.config.ResourceThreadPoolConfig;
import com.webank.wecross.network.p2p.netty.common.Node;
import com.webank.wecross.peer.Peer;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.resource.ResourceBlockHeaderManagerFactory;
import com.webank.wecross.storage.BlockHeaderStorageFactory;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.StubFactory;
import com.webank.wecross.stubmanager.StubManager;
import com.webank.wecross.zone.Chain;
import com.webank.wecross.zone.Zone;
import com.webank.wecross.zone.ZoneManager;
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

        BlockHeaderStorageFactory blockHeaderStorageFactory =
                Mockito.spy(BlockHeaderStorageFactory.class);
        Mockito.when(blockHeaderStorageFactory.newBlockHeaderStorage("payment.bcos"))
                .thenReturn(null);
        Mockito.when(blockHeaderStorageFactory.newBlockHeaderStorage("payment.bcos0"))
                .thenReturn(null);
        ResourceThreadPoolConfig.ResourceThreadPool resourceThreadPool =
                new ResourceThreadPoolConfig.ResourceThreadPool(10, 10, 200);
        ResourceBlockHeaderManagerFactory resourceBlockHeaderManagerFactory =
                new ResourceBlockHeaderManagerFactory(resourceThreadPool);

        ZoneManager zoneManager = new ZoneManager();
        zoneManager.setStubManager(stubManager);
        zoneManager.setBlockHeaderStorageFactory(blockHeaderStorageFactory);
        zoneManager.setResourceBlockHeaderManagerFactory(resourceBlockHeaderManagerFactory);

        assertEquals(stubManager, zoneManager.getStubManager());

        Peer peer = new Peer(new Node("aaa", "127.0.0.1", 100));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                ResourceInfo resourceInfo = new ResourceInfo();
                resourceInfo.setName("contract" + j);
                String path = "payment.bcos" + i + ".contract" + j;
                resourceInfo.setStubType("test");
                resourceInfo.setChecksum(resourceInfo.getName());

                Map<String, ResourceInfo> resources = new HashMap<String, ResourceInfo>();
                resources.put(path, resourceInfo);
                zoneManager.addRemoteResources(peer, resources);
            }
        }

        Map<String, ResourceInfo> allResourcesName = zoneManager.getAllResourcesInfo(false);
        System.out.println(allResourcesName);
        Assert.assertEquals(12, allResourcesName.size());

        Map<String, ResourceInfo> allResources = zoneManager.getAllResourcesInfo(false);
        Assert.assertEquals(12, allResources.size());

        Assert.assertEquals(1, zoneManager.getSeq());

        Map<String, ResourceInfo> allLocalResources = zoneManager.getAllResourcesInfo(true);
        System.out.println(allLocalResources);
        Assert.assertEquals(0, allLocalResources.size());

        // test for wrong path
        ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.setName("bcos");
        resourceInfo.setStubType("test");
        resourceInfo.setChecksum(resourceInfo.getName());

        Map<String, ResourceInfo> resources = new HashMap<String, ResourceInfo>();
        resources.put("payment.bcos", resourceInfo);
        zoneManager.addRemoteResources(peer, resources);

        Map<String, ResourceInfo> allResources2 = zoneManager.getAllResourcesInfo(false);
        System.out.println(allResources2);
        Assert.assertEquals(12, allResources2.size());

        // test for different peer
        ResourceInfo resourceInfo2 = new ResourceInfo();
        resourceInfo2.setName("contract0");
        resourceInfo2.setStubType("test");
        resourceInfo2.setChecksum(resourceInfo2.getName());

        Peer peer2 = new Peer(new Node("bbb", "127.0.0.1", 100));

        Map<String, ResourceInfo> resources2 = new HashMap<String, ResourceInfo>();
        resources2.put("payment.bcos0.contract0", resourceInfo2);
        zoneManager.addRemoteResources(peer2, resources2);

        Map<String, ResourceInfo> allResources3 = zoneManager.getAllResourcesInfo(false);
        System.out.println(allResources3);
        Assert.assertEquals(12, allResources3.size());

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

        BlockHeaderStorageFactory blockHeaderStorageFactory =
                Mockito.spy(BlockHeaderStorageFactory.class);
        Mockito.when(blockHeaderStorageFactory.newBlockHeaderStorage("payment.bcos"))
                .thenReturn(null);
        Mockito.when(blockHeaderStorageFactory.newBlockHeaderStorage("payment.bcos0"))
                .thenReturn(null);
        Mockito.when(blockHeaderStorageFactory.newBlockHeaderStorage("payment.bcos1"))
                .thenReturn(null);
        Mockito.when(blockHeaderStorageFactory.newBlockHeaderStorage("payment.bcos2"))
                .thenReturn(null);
        ResourceThreadPoolConfig.ResourceThreadPool resourceThreadPool =
                new ResourceThreadPoolConfig.ResourceThreadPool(10, 10, 200);
        ResourceBlockHeaderManagerFactory resourceBlockHeaderManagerFactory =
                new ResourceBlockHeaderManagerFactory(resourceThreadPool);

        ZoneManager zoneManager = new ZoneManager();
        zoneManager.setStubManager(stubManager);
        zoneManager.setBlockHeaderStorageFactory(blockHeaderStorageFactory);
        zoneManager.setResourceBlockHeaderManagerFactory(resourceBlockHeaderManagerFactory);

        Peer peer = new Peer(new Node("aaa", "127.0.0.1", 100));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                ResourceInfo resourceInfo = new ResourceInfo();
                resourceInfo.setName("contract" + j);
                resourceInfo.setStubType("test");
                resourceInfo.setChecksum(resourceInfo.getName());

                Map<String, ResourceInfo> resources = new HashMap<String, ResourceInfo>();
                resources.put("payment.bcos" + i + ".contract" + j, resourceInfo);
                zoneManager.addRemoteResources(peer, resources);
            }
        }

        Map<String, ResourceInfo> allResources = zoneManager.getAllResourcesInfo(false);
        Assert.assertEquals(12, allResources.size());

        ResourceInfo removeResource = new ResourceInfo();
        removeResource.setName("contract0");
        removeResource.setChecksum(removeResource.getName());

        Map<String, ResourceInfo> removeResources = new HashMap<String, ResourceInfo>();
        removeResources.put("payment.bcos0.contract0", removeResource);

        zoneManager.removeRemoteResources(peer, removeResources);

        allResources = zoneManager.getAllResourcesInfo(false);
        Assert.assertEquals(11, allResources.size());

        ResourceInfo resourceInfo2 = new ResourceInfo();
        resourceInfo2.setName("contract1");
        resourceInfo2.setStubType("test");
        resourceInfo2.setChecksum(resourceInfo2.getName());

        Peer peer2 = new Peer(new Node("bbb", "127.0.0.1", 100));

        Map<String, ResourceInfo> resources2 = new HashMap<String, ResourceInfo>();
        resources2.put("payment.bcos1.contract1", resourceInfo2);
        zoneManager.addRemoteResources(peer2, resources2);

        // removeResource.setName("payment.bcos1.contract1");
        removeResources.clear();
        removeResources.put("payment.bcos1.contract1", removeResource);
        zoneManager.removeRemoteResources(peer2, removeResources);

        allResources = zoneManager.getAllResourcesInfo(false);
        Assert.assertEquals(11, allResources.size());

        Resource resource = zoneManager.getResource(Path.decode("payment.bcos1.contract1"));
        // Assert.assertEquals(1, resource.getPeers().size());
        // Assert.assertTrue(resource.getPeers().contains(peer));
        // Assert.assertFalse(resource.getPeers().contains(peer2));

        for (int j = 0; j < 4; j++) {
            ResourceInfo resourceInfo = new ResourceInfo();
            resourceInfo.setName("contract" + j);
            resourceInfo.setChecksum("fakeChecksum" + j);
            resourceInfo.setChecksum(resourceInfo.getName());

            Map<String, ResourceInfo> resources = new HashMap<String, ResourceInfo>();
            resources.put("payment.bcos2" + ".contract" + j, resourceInfo);
            zoneManager.removeRemoteResources(peer, resources);
        }

        Zone zone = zoneManager.getZone("payment");
        Chain stub = zone.getChain("bcos2");
        Assert.assertNull(stub);

        // fatal test
        ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.setName("payment.bcos2.aaa");
        resourceInfo.setChecksum(resourceInfo.getName());

        Map<String, ResourceInfo> resources = new HashMap<String, ResourceInfo>();
        resources.put("payment.bcos2.aaa", resourceInfo);
        zoneManager.removeRemoteResources(peer2, resources);

        resources.clear();
        resources.put("payment.bcos1.contract0", resourceInfo);
        // resourceInfo.setName("payment.bcos1.contract0");
        zoneManager.removeRemoteResources(peer2, resources);

        resources.clear();
        resources.put("payment.bcos1.abc", resourceInfo);
        // resourceInfo.setName("payment.bcos1.abc");
        zoneManager.removeRemoteResources(peer2, resources);

        resources.clear();
        resources.put("payment1.bcos4.abc", resourceInfo);
        // resourceInfo.setName("payment1.bcos4.abc");
        zoneManager.removeRemoteResources(peer2, resources);

        resources.clear();
        resources.put("payment.abc", resourceInfo);
        // resourceInfo.setName("payment.abc");
        zoneManager.removeRemoteResources(peer2, resources);

        for (Map.Entry<String, ResourceInfo> entry :
                zoneManager.getAllResourcesInfo(false).entrySet()) {
            resources = new HashMap<String, ResourceInfo>();
            resources.put(entry.getKey(), entry.getValue());
            zoneManager.removeRemoteResources(peer, resources);
        }

        Assert.assertEquals(0, zoneManager.getZones().size());
    }

    @Test
    public void checksumTest() throws Exception {
        StubFactory stubFactory = Mockito.spy(StubFactory.class);
        Mockito.when(stubFactory.newConnection(Mockito.anyString())).thenReturn(null);
        Mockito.when(stubFactory.newDriver()).thenReturn(null);

        StubManager stubManager = Mockito.mock(StubManager.class);
        Mockito.when(stubManager.getStubFactory("test")).thenReturn(stubFactory);

        BlockHeaderStorageFactory blockHeaderStorageFactory =
                Mockito.spy(BlockHeaderStorageFactory.class);
        Mockito.when(blockHeaderStorageFactory.newBlockHeaderStorage("payment.bcos"))
                .thenReturn(null);

        ResourceThreadPoolConfig.ResourceThreadPool resourceThreadPool =
                new ResourceThreadPoolConfig.ResourceThreadPool(10, 10, 200);
        ResourceBlockHeaderManagerFactory resourceBlockHeaderManagerFactory =
                new ResourceBlockHeaderManagerFactory(resourceThreadPool);

        ZoneManager zoneManager = new ZoneManager();
        zoneManager.setStubManager(stubManager);
        zoneManager.setBlockHeaderStorageFactory(blockHeaderStorageFactory);
        zoneManager.setResourceBlockHeaderManagerFactory(resourceBlockHeaderManagerFactory);

        // Add resource 1
        String path = "payment.bcos.aaaaa";
        ResourceInfo resourceInfo1 = new ResourceInfo();
        resourceInfo1.setStubType("test");
        resourceInfo1.setName(path);
        resourceInfo1.setChecksum("aaaaa");
        Peer peer = new Peer(new Node("bbb", "127.0.0.1", 100));
        Map<String, ResourceInfo> resources1 = new HashMap<String, ResourceInfo>();
        resources1.put(path, resourceInfo1);

        zoneManager.addRemoteResources(peer, resources1);
        Assert.assertTrue(zoneManager.getResource(Path.decode(path)) != null);

        // Add same path diff checksum resource
        ResourceInfo resourceInfo2 = new ResourceInfo();
        resourceInfo1.setStubType("test");
        resourceInfo2.setName(path);
        resourceInfo2.setChecksum("bbbbb");

        Map<String, ResourceInfo> resources2 = new HashMap<String, ResourceInfo>();
        resources2.put(path, resourceInfo2);

        zoneManager.addRemoteResources(peer, resources2);
        Assert.assertTrue(zoneManager.getResource(Path.decode(path)) != null);
    }
}
