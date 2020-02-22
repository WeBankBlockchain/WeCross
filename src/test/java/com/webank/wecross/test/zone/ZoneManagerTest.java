package com.webank.wecross.test.zone;

import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.netty.common.Node;
import com.webank.wecross.peer.Peer;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.resource.ResourceInfo;
import com.webank.wecross.stub.Stub;
import com.webank.wecross.zone.Zone;
import com.webank.wecross.zone.ZoneManager;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZoneManagerTest {
    private Peer mockPeer = new Peer(new Node());

    private P2PMessageEngine p2pEngine = null;

    private Logger logger = LoggerFactory.getLogger(ZoneManagerTest.class);

    @Test
    public void addRemoteResourcesTest() throws Exception {
        ZoneManager zoneManager = Mockito.spy(ZoneManager.class);
        
        Peer peer = new Peer(new Node("aaa", "127.0.0.1", 100));
        
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                ResourceInfo resourceInfo = new ResourceInfo();
                resourceInfo.setPath("payment.bcos" + i + ".contract" + j);
                resourceInfo.setDistance(i); // i == 0, set it as local resource
                
                Set<ResourceInfo> resources = new HashSet<ResourceInfo>();
                resources.add(resourceInfo);
                zoneManager.addRemoteResources(peer, resources);
            }
        }

        Set<String> allResources = zoneManager.getAllNetworkStubResourceName(false);
        System.out.println(allResources);
        Assert.assertEquals(12, allResources.size());

        Set<String> allLocalResources = zoneManager.getAllNetworkStubResourceName(true);
        System.out.println(allLocalResources);
        Assert.assertEquals(4, allLocalResources.size());
        
        //test for wrong path
        ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.setPath("payment.bcos");
        resourceInfo.setDistance(0); // i == 0, set it as local resource
        
        Set<ResourceInfo> resources = new HashSet<ResourceInfo>();
        resources.add(resourceInfo);
        zoneManager.addRemoteResources(peer, resources);
        
        Set<String> allResources2 = zoneManager.getAllNetworkStubResourceName(false);
        System.out.println(allResources2);
        Assert.assertEquals(12, allResources2.size());
        
        //test for different peer
        ResourceInfo resourceInfo2 = new ResourceInfo();
        resourceInfo2.setPath("payment.bcos0.contract0");
        resourceInfo2.setDistance(0); // i == 0, set it as local resource
        
        Peer peer2 = new Peer(new Node("bbb", "127.0.0.1", 100));
        
        Set<ResourceInfo> resources2 = new HashSet<ResourceInfo>();
        resources2.add(resourceInfo2);
        zoneManager.addRemoteResources(peer2, resources2);
        
        Set<String> allResources3 = zoneManager.getAllNetworkStubResourceName(false);
        System.out.println(allResources3);
        Assert.assertEquals(12, allResources3.size());
        
        Resource resource2 = zoneManager.getResource(Path.decode("payment.bcos0.contract0"));
        Assert.assertEquals(2, resource2.getPeers().size());
    }
    
    @Test
    public void removeRemoteResourcesTest() throws Exception {
    	ZoneManager zoneManager = Mockito.spy(ZoneManager.class);
        
        Peer peer = new Peer(new Node("aaa", "127.0.0.1", 100));
        
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                ResourceInfo resourceInfo = new ResourceInfo();
                resourceInfo.setPath("payment.bcos" + i + ".contract" + j);
                resourceInfo.setDistance(i); // i == 0, set it as local resource
                
                Set<ResourceInfo> resources = new HashSet<ResourceInfo>();
                resources.add(resourceInfo);
                zoneManager.addRemoteResources(peer, resources);
            }
        }
        
        Set<String> allResources = zoneManager.getAllNetworkStubResourceName(false);
        Assert.assertEquals(12, allResources.size());
        
        ResourceInfo removeResource = new ResourceInfo();
        removeResource.setPath("payment.bcos0.contract0");
        removeResource.setDistance(0);
        
        Set<ResourceInfo> removeResources = new HashSet<ResourceInfo>();
        removeResources.add(removeResource);
        
        zoneManager.removeRemoteResources(peer, removeResources);
        
        allResources = zoneManager.getAllNetworkStubResourceName(false);
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
        
        allResources = zoneManager.getAllNetworkStubResourceName(false);
        Assert.assertEquals(11, allResources.size());
        
        Resource resource = zoneManager.getResource(Path.decode("payment.bcos1.contract1"));
        Assert.assertEquals(1, resource.getPeers().size());
        Assert.assertTrue(resource.getPeers().contains(peer));
        Assert.assertFalse(resource.getPeers().contains(peer2));
     
        for (int j = 0; j < 4; j++) {
	        ResourceInfo resourceInfo = new ResourceInfo();
	        resourceInfo.setPath("payment.bcos2" + ".contract" + j);
	        resourceInfo.setDistance(2); // i == 0, set it as local resource
	        
	        Set<ResourceInfo> resources = new HashSet<ResourceInfo>();
	        resources.add(resourceInfo);
	        zoneManager.removeRemoteResources(peer, resources);
        }
        
        Zone zone = zoneManager.getNetwork("payment");
        Stub stub = zone.getStub("bcos2");
        Assert.assertNull(stub);
        
        //fatal test
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
        
        for(Map.Entry<String, ResourceInfo> entry: zoneManager.getAllNetworkStubResourceInfo(false).entrySet()) {
        	resources = new HashSet<ResourceInfo>();
        	resources.add(entry.getValue());
        	zoneManager.removeRemoteResources(peer, resources);
        }
        
        Assert.assertEquals(0, zoneManager.getNetworks().size());
    }

    /*
    @Test
    public void resourceUpdatePeerNetworkTest() throws Exception {
        ZoneManager networkManager = new ZoneManager();

        for (int i = 0; i < 2; i++) {
            RemoteResource resource = new RemoteResource(mockPeer, 1, p2pEngine);
            resource.setPath(Path.decode("old.bcos.contract" + i));
            resource.setDistance(1); // i == 0, set it as local resource
            networkManager.addResource(resource);
        }
        Assert.assertEquals(2, networkManager.getAllNetworkStubResourceName(false).size());

        Set<PeerInfo> activePeers = new HashSet<>();
        PeerResources peerResources = new PeerResources(activePeers);
        PeerInfo peer0 = new PeerInfo(new Node("peer0", "", 0));
        activePeers.add(peer0);
        Set<ResourceInfo> activeResourceInfos = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            RemoteResource resource = new RemoteResource(mockPeer, 1, p2pEngine);
            String resourceName = "current.bcos.contract" + i;
            activeResourceInfos.add(new ResourceInfo(resourceName));
            resource.setPath(Path.decode(resourceName));
            resource.setDistance(1); // i == 0, set it as local resource
            networkManager.addResource(resource);
        }
        peer0.setResources(100, activeResourceInfos);
        Assert.assertEquals(5, networkManager.getAllNetworkStubResourceName(false).size());

        PeerInfo peer1 = new PeerInfo(new Node("peer1", "", 0));
        activePeers.add(peer1);
        Set<ResourceInfo> newResourceInfos = new HashSet<>();
        for (int i = 0; i < 4; i++) {
            RemoteResource resource = new RemoteResource(mockPeer, 1, p2pEngine);
            String resourceName = "new.bcos.contract" + i;
            newResourceInfos.add(new ResourceInfo(resourceName));
        }
        peer1.setResources(100, newResourceInfos);

        networkManager.updateActivePeerNetwork(peerResources);
        Assert.assertEquals(7, networkManager.getAllNetworkStubResourceName(false).size());

        // Test resource contain more peers
        for (int i = 0; i < 4; i++) {
            String resourceName = "new.bcos.contract" + i;
            Assert.assertEquals(
                    1, networkManager.getResource(Path.decode(resourceName)).getPeers().size());
        }

        activeResourceInfos.addAll(newResourceInfos);
        peer0.setResources(200, activeResourceInfos);
        peerResources.noteDirty();
        networkManager.updateActivePeerNetwork(peerResources);

        // Test resource contain more peers
        for (int i = 0; i < 4; i++) {
            String resourceName = "new.bcos.contract" + i;
            Assert.assertEquals(
                    2, networkManager.getResource(Path.decode(resourceName)).getPeers().size());
        }
    }

    private Set<PeerInfo> newMockInvalidPeerInfos() {
        ResourceInfo resourceInfo0 = new ResourceInfo();
        resourceInfo0.setDistance(0);
        resourceInfo0.setPath("network.stub.resource0");
        resourceInfo0.setChecksum("000000");

        ResourceInfo resourceInfo1 = new ResourceInfo();
        resourceInfo1.setDistance(0);
        resourceInfo1.setPath("network.stub.resource1");
        resourceInfo1.setChecksum("111111");

        ResourceInfo resourceInfo2 = new ResourceInfo();
        resourceInfo2.setDistance(0);
        resourceInfo2.setPath("network.stub.resource2");
        resourceInfo2.setChecksum("222222");

        ResourceInfo resourceInfo0Mock = new ResourceInfo();
        resourceInfo0Mock.setDistance(0);
        resourceInfo0Mock.setPath("network.stub.resource0");
        resourceInfo0Mock.setChecksum("666666"); // checksum is not the same as resourceInfo0

        PeerInfo info0 = new PeerInfo(new Node("peer0", "", 0));
        Set<ResourceInfo> resourceInfos0 = new HashSet<>();
        resourceInfos0.add(resourceInfo0);
        resourceInfos0.add(resourceInfo1);
        info0.setResourceInfos(resourceInfos0);

        PeerInfo info1 = new PeerInfo(new Node("peer1", "", 0));
        Set<ResourceInfo> resourceInfos1 = new HashSet<>();
        resourceInfos1.add(resourceInfo2);
        resourceInfos1.add(resourceInfo0Mock);
        info1.setResourceInfos(resourceInfos1);

        Set<PeerInfo> ret = new HashSet<>();
        ret.add(info0);
        ret.add(info1);
        return ret;
    }

    @Test
    public void updateActivePeerNetworkTest() throws Exception {
        ZoneManager networkManager = new ZoneManager();
        PeerResources peerResources = PeerResourcesTest.newMockPeerInfo();

        Resource localResource = new TestResource();
        localResource.setPath(Path.decode("network.stub.resource1"));
        networkManager.addResource(localResource);

        networkManager.updateActivePeerNetwork(peerResources);

        Set<String> ret = networkManager.getAllNetworkStubResourceName(false);

        Assert.assertEquals(2, ret.size());
        Assert.assertTrue(ret.contains("network.stub.resource1"));
        Assert.assertTrue(ret.contains("network.stub.resource2"));
    }

    @Test
    public void addAndRemoveConcurrentTest() {
        try {
            ZoneManager networkManager = new ZoneManager();
            Set<Thread> threads = new HashSet<>();
            for (int i = 0; i < 8; i++) {
                Runnable runnable =
                        new Runnable() {
                            public void run() {
                                try {
                                    SecureRandom rand = new SecureRandom();
                                    if (rand.nextBoolean()) {
                                        addSomeTestResources(networkManager, 128);
                                    } else {
                                        removeSomeTestResources(networkManager, 128);
                                    }
                                } catch (Exception e) {
                                    System.out.println("Thread exception:" + e);
                                    Assert.assertTrue(e.getMessage(), false);
                                }
                            }
                        };

                Thread thread = new Thread(runnable);
                threads.add(thread);
                thread.start();
            }

            for (Thread thread : threads) {
                // System.out.println("waiting thread");
                thread.join();
            }
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }
    }

    private void addSomeTestResources(ZoneManager networkManager, int num) throws Exception {
        try {
            logger.info("Add resource");
            List<Integer> idList = new ArrayList<>();
            for (int i = 0; i < num; i++) {
                idList.add(i);
            }
            Collections.shuffle(idList);
            for (int i : idList) {
                String name =
                        "test-network"
                                + (i / 100)
                                + ".test-stub"
                                + ((i / 10) % 10)
                                + ".test-resource"
                                + i % 10;
                Path path = Path.decode(name);
                com.webank.wecross.resource.Resource resource = new TestResource();
                resource.setPath(path);
                networkManager.addResource(resource);
            }
        } catch (Exception e) {
            logger.warn("Add resource exception " + e);
            throw new Exception("Add resource exception " + e.getLocalizedMessage());
        }
    }

    private void removeSomeTestResources(ZoneManager networkManager, int num) throws Exception {

        logger.info("Remove resource");
        List<Integer> idList = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            idList.add(i);
        }
        Collections.shuffle(idList);
        for (int i : idList) {
            String name =
                    "test-network"
                            + (i / 100)
                            + ".test-stub"
                            + ((i / 10) % 10)
                            + ".test-resource"
                            + i % 10;
            Path path = Path.decode(name);
            networkManager.removeResource(path);
        }
    }
    */
}
