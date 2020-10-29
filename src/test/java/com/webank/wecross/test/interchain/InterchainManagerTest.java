package com.webank.wecross.test.interchain;

import com.webank.wecross.account.AccountManager;
import com.webank.wecross.interchain.InterchainManager;
import com.webank.wecross.polling.TaskManager;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.Path;
import com.webank.wecross.zone.ChainInfo;
import com.webank.wecross.zone.ZoneManager;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class InterchainManagerTest {
    @Test
    public void registerTaskTest() throws Exception {
        ZoneManager mockZoneManager = Mockito.mock(ZoneManager.class);
        Map<String, ChainInfo> chainInfoMap = new HashMap<>();
        chainInfoMap.put("test.test", new ChainInfo());
        Resource resource = new Resource();
        resource.setPath(Path.decode("test.test.test"));
        Mockito.when(mockZoneManager.getAllChainsInfo(true)).thenReturn(chainInfoMap);
        Mockito.when(mockZoneManager.fetchResource(Mockito.any())).thenReturn(resource);

        AccountManager mockAccountManager = Mockito.mock(AccountManager.class);
        TaskManager taskManager = new TaskManager();
        InterchainManager interchainManager = new InterchainManager();
        interchainManager.setAccountManager(mockAccountManager);
        interchainManager.setZoneManager(mockZoneManager);
        interchainManager.registerTask(taskManager);
        taskManager.start();

        Assert.assertEquals(1, taskManager.getTasks().size());
    }
}
