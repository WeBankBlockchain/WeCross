package com.webank.wecross.test.network;

import com.webank.wecross.network.NetworkManager;
import java.util.Set;
import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class NetworkManagerTest {
    @Resource NetworkManager networkManager;

    @Test
    public void getAllNetworkStubResourceNameTest() {
        Set<String> resources = networkManager.getAllNetworkStubResourceName();
        System.out.println(resources);
        Assert.assertTrue(resources.contains("payment/bcos1/HelloWorldContract"));
        Assert.assertTrue(resources.contains("bill/bcos1/HelloWorldContract"));
        Assert.assertTrue(resources.contains("payment/bcos2/HelloWorldContract"));
    }
}
