package com.webank.wecross.test.network;

import com.webank.wecross.network.NetworkManager;
import com.webank.wecross.resource.EventCallback;
import com.webank.wecross.resource.GetDataRequest;
import com.webank.wecross.resource.GetDataResponse;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.SetDataRequest;
import com.webank.wecross.resource.SetDataResponse;
import com.webank.wecross.resource.TransactionRequest;
import com.webank.wecross.resource.TransactionResponse;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class NetworkManagerTest {
    class MockResource implements com.webank.wecross.resource.Resource {
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

        @Override
        public boolean isLocal() {
            return getDistance() == 0;
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
}
