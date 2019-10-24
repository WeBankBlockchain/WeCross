package com.webank.wecross.test.network;

import com.webank.wecross.host.Peer;
import com.webank.wecross.network.NetworkManager;
import com.webank.wecross.resource.Path;
import com.webank.wecross.stub.remote.RemoteResource;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

public class NetworkManagerTest {
    private Peer mockPeer = new Peer();

    @Test
    public void resourceAddAndRemoveTest() throws Exception {
        NetworkManager networkManager = new NetworkManager();
        // Add test some are local
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                RemoteResource resource = new RemoteResource(mockPeer, 1);
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
                RemoteResource resource = new RemoteResource(mockPeer, 1);
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
            RemoteResource resource = new RemoteResource(mockPeer, 1);
            resource.setPath(Path.decode("old.bcos.contract" + i));
            resource.setDistance(1); // i == 0, set it as local resource
            networkManager.addResource(resource);
        }
        Assert.assertEquals(2, networkManager.getAllNetworkStubResourceName(false).size());

        Set<Peer> activePeers = new HashSet<>();
        Peer peer0 = new Peer();
        activePeers.add(peer0);
        Set<String> activeResourcesname = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            RemoteResource resource = new RemoteResource(mockPeer, 1);
            String resourceName = "current.bcos.contract" + i;
            activeResourcesname.add(resourceName);
            resource.setPath(Path.decode(resourceName));
            resource.setDistance(1); // i == 0, set it as local resource
            networkManager.addResource(resource);
        }
        peer0.setResources(100, activeResourcesname);
        Assert.assertEquals(5, networkManager.getAllNetworkStubResourceName(false).size());

        Peer peer1 = new Peer();
        activePeers.add(peer1);
        Set<String> newResourcesname = new HashSet<>();
        for (int i = 0; i < 4; i++) {
            RemoteResource resource = new RemoteResource(mockPeer, 1);
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
}
