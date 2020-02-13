package com.webank.wecross.test.peer;

import com.webank.wecross.network.NetworkManager;
import com.webank.wecross.p2p.netty.common.Node;
import com.webank.wecross.peer.PeerInfo;
import com.webank.wecross.peer.PeerResources;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.resource.ResourceInfo;
import com.webank.wecross.resource.TestResource;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

public class PeerResourcesTest {

    public static PeerResources newMockPeerInfo() {
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

        Set<PeerInfo> peerInfos = new HashSet<>();
        peerInfos.add(info0);
        peerInfos.add(info1);
        PeerResources peerResources = new PeerResources(peerInfos);
        return peerResources;
    }

    @Test
    public void noMyselfTest() {
        PeerResources peerResources = newMockPeerInfo();
        Assert.assertEquals(0, peerResources.getResource2Checksum().size());
        Assert.assertEquals(0, peerResources.getResource2Peers().size());
    }

    @Test
    public void hasNullMyselfTest() throws Exception {
        PeerResources peerResources = newMockPeerInfo();
        peerResources.updateMyselfResources(null);

        peerResources.loggingInvalidResources();

        Assert.assertEquals(2, peerResources.getResource2Checksum().size());
        Assert.assertEquals(2, peerResources.getResource2Peers().size());
    }

    @Test
    public void hasMyselfTest() throws Exception {
        PeerResources peerResources = newMockPeerInfo();
        NetworkManager networkManager = new NetworkManager();

        Resource resource = new TestResource();
        resource.setPath(Path.decode("network.stub.resource1"));
        networkManager.addResource(resource);
        peerResources.updateMyselfResources(networkManager.getAllNetworkStubResourceInfo(true));

        peerResources.loggingInvalidResources();

        Assert.assertEquals(1, peerResources.getResource2Checksum().size());
        Assert.assertEquals(1, peerResources.getResource2Peers().size());
    }
}
