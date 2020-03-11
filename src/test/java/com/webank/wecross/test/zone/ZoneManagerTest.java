package com.webank.wecross.test.zone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.webank.wecross.chain.Chain;
import com.webank.wecross.p2p.netty.common.Node;
import com.webank.wecross.peer.Peer;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.resource.ResourceInfo;
import com.webank.wecross.stub.StubFactory;
import com.webank.wecross.stub.StubManager;
import com.webank.wecross.zone.Zone;
import com.webank.wecross.zone.ZoneManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

        StubManager stubManager = Mockito.spy(StubManager.class);
        Mockito.when(stubManager.getStubFactory(Mockito.anyString())).thenReturn(stubFactory);

        ZoneManager zoneManager = new ZoneManager();
        zoneManager.setStubManager(stubManager);

        assertEquals(stubManager, zoneManager.getStubManager());

        Peer peer = new Peer(new Node("aaa", "127.0.0.1", 100));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                ResourceInfo resourceInfo = new ResourceInfo();
                resourceInfo.setPath("payment.bcos" + i + ".contract" + j);
                resourceInfo.setDistance(i); // i == 0, set it as local resource
                resourceInfo.setStubType("test");

                Set<ResourceInfo> resources = new HashSet<ResourceInfo>();
                resources.add(resourceInfo);
                zoneManager.addRemoteResources(peer, resources);
            }
        }

        Set<String> allResourcesName = zoneManager.getAllResourceName(false);
        System.out.println(allResourcesName);
        Assert.assertEquals(12, allResourcesName.size());

        List<Resource> allResources = zoneManager.getAllResources(false);
        Assert.assertEquals(12, allResources.size());

        Assert.assertEquals(13, zoneManager.getSeq());

        Set<String> allLocalResources = zoneManager.getAllResourceName(true);
        System.out.println(allLocalResources);
        Assert.assertEquals(4, allLocalResources.size());

        // test for wrong path
        ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.setPath("payment.bcos");
        resourceInfo.setDistance(0); // i == 0, set it as local resource
        resourceInfo.setStubType("test");

        Set<ResourceInfo> resources = new HashSet<ResourceInfo>();
        resources.add(resourceInfo);
        zoneManager.addRemoteResources(peer, resources);

        Set<String> allResources2 = zoneManager.getAllResourceName(false);
        System.out.println(allResources2);
        Assert.assertEquals(12, allResources2.size());

        // test for different peer
        ResourceInfo resourceInfo2 = new ResourceInfo();
        resourceInfo2.setPath("payment.bcos0.contract0");
        resourceInfo2.setDistance(0); // i == 0, set it as local resource

        Peer peer2 = new Peer(new Node("bbb", "127.0.0.1", 100));

        Set<ResourceInfo> resources2 = new HashSet<ResourceInfo>();
        resources2.add(resourceInfo2);
        zoneManager.addRemoteResources(peer2, resources2);

        Set<String> allResources3 = zoneManager.getAllResourceName(false);
        System.out.println(allResources3);
        Assert.assertEquals(12, allResources3.size());

        Resource resource2 = zoneManager.getResource(Path.decode("payment.bcos0.contract0"));
        // Assert.assertEquals(2, resource2.getPeers().size());
    }

    @Test
    public void removeRemoteResourcesTest() throws Exception {
        StubFactory stubFactory = Mockito.spy(StubFactory.class);
        Mockito.when(stubFactory.newConnection(Mockito.anyString())).thenReturn(null);
        Mockito.when(stubFactory.newDriver()).thenReturn(null);

        StubManager stubManager = Mockito.spy(StubManager.class);
        Mockito.when(stubManager.getStubFactory("test")).thenReturn(stubFactory);

        ZoneManager zoneManager = new ZoneManager();
        zoneManager.setStubManager(stubManager);

        Peer peer = new Peer(new Node("aaa", "127.0.0.1", 100));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                ResourceInfo resourceInfo = new ResourceInfo();
                resourceInfo.setPath("payment.bcos" + i + ".contract" + j);
                resourceInfo.setDistance(i); // i == 0, set it as local resource
                resourceInfo.setStubType("test");

                Set<ResourceInfo> resources = new HashSet<ResourceInfo>();
                resources.add(resourceInfo);
                zoneManager.addRemoteResources(peer, resources);
            }
        }

        Set<String> allResources = zoneManager.getAllResourceName(false);
        Assert.assertEquals(12, allResources.size());

        ResourceInfo removeResource = new ResourceInfo();
        removeResource.setPath("payment.bcos0.contract0");
        removeResource.setDistance(0);

        Set<ResourceInfo> removeResources = new HashSet<ResourceInfo>();
        removeResources.add(removeResource);

        zoneManager.removeRemoteResources(peer, removeResources);

        allResources = zoneManager.getAllResourceName(false);
        Assert.assertEquals(11, allResources.size());

        ResourceInfo resourceInfo2 = new ResourceInfo();
        resourceInfo2.setPath("payment.bcos1.contract1");
        resourceInfo2.setDistance(0); // i == 0, set it as local resource

        Peer peer2 = new Peer(new Node("bbb", "127.0.0.1", 100));

        Set<ResourceInfo> resources2 = new HashSet<ResourceInfo>();
        resources2.add(resourceInfo2);
        zoneManager.addRemoteResources(peer2, resources2);

        removeResource.setPath("payment.bcos1.contract1");
        zoneManager.removeRemoteResources(peer2, removeResources);

        allResources = zoneManager.getAllResourceName(false);
        Assert.assertEquals(11, allResources.size());

        Resource resource = zoneManager.getResource(Path.decode("payment.bcos1.contract1"));
        // Assert.assertEquals(1, resource.getPeers().size());
        // Assert.assertTrue(resource.getPeers().contains(peer));
        // Assert.assertFalse(resource.getPeers().contains(peer2));

        for (int j = 0; j < 4; j++) {
            ResourceInfo resourceInfo = new ResourceInfo();
            resourceInfo.setPath("payment.bcos2" + ".contract" + j);
            resourceInfo.setDistance(2); // i == 0, set it as local resource

            Set<ResourceInfo> resources = new HashSet<ResourceInfo>();
            resources.add(resourceInfo);
            zoneManager.removeRemoteResources(peer, resources);
        }

        Zone zone = zoneManager.getZone("payment");
        Chain stub = zone.getStub("bcos2");
        Assert.assertNull(stub);

        // fatal test
        ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.setPath("payment.bcos2.aaa");
        resourceInfo.setDistance(2);

        Set<ResourceInfo> resources = new HashSet<ResourceInfo>();
        resources.add(resourceInfo);
        zoneManager.removeRemoteResources(peer2, resources);

        resourceInfo.setPath("payment.bcos1.contract0");
        zoneManager.removeRemoteResources(peer2, resources);

        resourceInfo.setPath("payment.bcos1.abc");
        zoneManager.removeRemoteResources(peer2, resources);

        resourceInfo.setPath("payment1.bcos4.abc");
        zoneManager.removeRemoteResources(peer2, resources);

        resourceInfo.setPath("payment.abc");
        zoneManager.removeRemoteResources(peer2, resources);

        for (Map.Entry<String, ResourceInfo> entry :
                zoneManager.getAllResourceInfo(false).entrySet()) {
            resources = new HashSet<ResourceInfo>();
            resources.add(entry.getValue());
            zoneManager.removeRemoteResources(peer, resources);
        }

        Assert.assertEquals(0, zoneManager.getZones().size());
    }
}
