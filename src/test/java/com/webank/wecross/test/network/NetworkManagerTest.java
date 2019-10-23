package com.webank.wecross.test.network;

import com.webank.wecross.host.Peer;
import com.webank.wecross.network.NetworkManager;
import com.webank.wecross.resource.EventCallback;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.resource.request.GetDataRequest;
import com.webank.wecross.resource.request.SetDataRequest;
import com.webank.wecross.resource.request.TransactionRequest;
import com.webank.wecross.resource.response.GetDataResponse;
import com.webank.wecross.resource.response.SetDataResponse;
import com.webank.wecross.resource.response.TransactionResponse;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

public class NetworkManagerTest {

    class MockResource implements Resource {

        private Path path;
        private int distance = 0;

        @Override
        public Path getPath() {
            return path;
        }

        @Override
        public void setPath(Path path) {
            this.path = path;
        }

        @Override
        public String getPathAsString() {
            return null;
        }

        @Override
        public Set<Peer> getPeers() {
            return null;
        }

        @Override
        public void setPeers(Set<Peer> peers) {}

        @Override
        public String getType() {
            return "MOCK_RESOURCE";
        }

        @Override
        public GetDataResponse getData(GetDataRequest request) {
            return null;
        }

        @Override
        public SetDataResponse setData(SetDataRequest request) {
            return null;
        }

        @Override
        public TransactionResponse call(TransactionRequest request) {
            return null;
        }

        @Override
        public TransactionResponse sendTransaction(TransactionRequest request) {
            return null;
        }

        @Override
        public void registerEventHandler(EventCallback callback) {}

        @Override
        public TransactionRequest createRequest() {
            return null;
        }

        @Override
        public int getDistance() {
            return distance;
        }

        public void setDistance(int accessDepth) {
            this.distance = accessDepth;
        }
    }

    @Test
    public void resourceAddAndRemoveTest() throws Exception {
        NetworkManager networkManager = new NetworkManager();

        // Add test some are local
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                MockResource resource = new MockResource();
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
                MockResource resource = new MockResource();
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
            MockResource resource = new MockResource();
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
            MockResource resource = new MockResource();
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
            MockResource resource = new MockResource();
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
