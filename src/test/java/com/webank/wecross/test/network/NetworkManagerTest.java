package com.webank.wecross.test.network;

import com.webank.wecross.network.NetworkManager;
import com.webank.wecross.p2p.P2PMessageEngine;
import com.webank.wecross.p2p.netty.common.Peer;
import com.webank.wecross.peer.PeerInfo;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.TestResource;
import com.webank.wecross.stub.remote.RemoteResource;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class NetworkManagerTest {
    private Peer mockPeer = new Peer();

    @Resource(name = "newP2PMessageEngine")
    private P2PMessageEngine p2pEngine;

    private Logger logger = LoggerFactory.getLogger(NetworkManagerTest.class);

    @Test
    public void resourceAddAndRemoveTest() throws Exception {
        NetworkManager networkManager = new NetworkManager();
        // Add test some are local
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                RemoteResource resource = new RemoteResource(mockPeer, 1, p2pEngine);
                resource.setPath(Path.decode("payment.bcos" + i + ".contract" + j));
                resource.setDistance(i); // i == 0, set it as local resource
                networkManager.addResource(resource);
            }
        }

        Set<String> allResources = networkManager.getAllNetworkStubResourceName(false);
        System.out.println(allResources);
        Assert.assertEquals(12, allResources.size());

        Set<String> allLocalResources = networkManager.getAllNetworkStubResourceName(true);
        System.out.println(allLocalResources);
        Assert.assertEquals(4, allLocalResources.size());

        // Update route test
        for (int i = 1; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                RemoteResource resource = new RemoteResource(mockPeer, 1, p2pEngine);
                resource.setPath(Path.decode("payment.bcos" + i + ".contract" + j));
                resource.setDistance(i - 1); // i == 0, set it as local resource
                networkManager.addResource(resource);
            }
        }

        allResources = networkManager.getAllNetworkStubResourceName(false);
        System.out.println(allResources);
        Assert.assertEquals(12, allResources.size());

        allLocalResources = networkManager.getAllNetworkStubResourceName(true);
        System.out.println(allLocalResources);
        Assert.assertEquals(8, allLocalResources.size());

        // Remove test
        int resourcesSize = allResources.size();
        for (String pathName : allResources) {
            networkManager.removeResource(Path.decode(pathName));
            resourcesSize--;
            Set<String> currentResources = networkManager.getAllNetworkStubResourceName(false);
            Assert.assertEquals(currentResources.size(), resourcesSize);
        }
    }

    @Test
    public void resourceUpdatePeerNetworkTest() throws Exception {
        NetworkManager networkManager = new NetworkManager();

        for (int i = 0; i < 2; i++) {
            RemoteResource resource = new RemoteResource(mockPeer, 1, p2pEngine);
            resource.setPath(Path.decode("old.bcos.contract" + i));
            resource.setDistance(1); // i == 0, set it as local resource
            networkManager.addResource(resource);
        }
        Assert.assertEquals(2, networkManager.getAllNetworkStubResourceName(false).size());

        Set<PeerInfo> activePeers = new HashSet<>();
        PeerInfo peer0 = new PeerInfo(new Peer("peer0"));
        activePeers.add(peer0);
        Set<String> activeResourcesname = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            RemoteResource resource = new RemoteResource(mockPeer, 1, p2pEngine);
            String resourceName = "current.bcos.contract" + i;
            activeResourcesname.add(resourceName);
            resource.setPath(Path.decode(resourceName));
            resource.setDistance(1); // i == 0, set it as local resource
            networkManager.addResource(resource);
        }
        peer0.setResources(100, activeResourcesname);
        Assert.assertEquals(5, networkManager.getAllNetworkStubResourceName(false).size());

        PeerInfo peer1 = new PeerInfo(new Peer("peer1"));
        activePeers.add(peer1);
        Set<String> newResourcesname = new HashSet<>();
        for (int i = 0; i < 4; i++) {
            RemoteResource resource = new RemoteResource(mockPeer, 1, p2pEngine);
            String resourceName = "new.bcos.contract" + i;
            newResourcesname.add(resourceName);
        }
        peer1.setResources(100, newResourcesname);

        networkManager.updateActivePeerNetwork(activePeers);
        Assert.assertEquals(7, networkManager.getAllNetworkStubResourceName(false).size());

        // Test resource contain more peers
        for (int i = 0; i < 4; i++) {
            String resourceName = "new.bcos.contract" + i;
            Assert.assertEquals(
                    networkManager.getResource(Path.decode(resourceName)).getPeers().size(), 1);
        }

        activeResourcesname.addAll(newResourcesname);
        peer0.setResources(200, activeResourcesname);
        networkManager.updateActivePeerNetwork(activePeers);

        // Test resource contain more peers
        for (int i = 0; i < 4; i++) {
            String resourceName = "new.bcos.contract" + i;
            Assert.assertEquals(
                    networkManager.getResource(Path.decode(resourceName)).getPeers().size(), 2);
        }
    }

    @Test
    public void addAndRemoveConcurrentTest() {
        try {
            NetworkManager networkManager = new NetworkManager();
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

    private void addSomeTestResources(NetworkManager networkManager, int num) throws Exception {
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

    private void removeSomeTestResources(NetworkManager networkManager, int num) throws Exception {

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
}
