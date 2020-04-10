package com.webank.wecross.test.routine;

import com.webank.wecross.account.AccountManager;
import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.routine.RoutineExecutor;
import com.webank.wecross.routine.RoutineManager;
import com.webank.wecross.routine.htlc.HTLCManager;
import com.webank.wecross.routine.htlc.HTLCResourcePair;
import com.webank.wecross.routine.htlc.HTLCTaskInfo;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Path;
import com.webank.wecross.zone.ZoneManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class RoutineExecutorTest {
    @Test
    public void checkHtlcResourcesTest() throws Exception {
        WeCrossHost mockWeCrossHost = Mockito.mock(WeCrossHost.class);
        ZoneManager mockZoneManager = Mockito.mock(ZoneManager.class);
        Resource mockResource = Mockito.mock(Resource.class);
        Mockito.when(mockWeCrossHost.getZoneManager()).thenReturn(mockZoneManager);
        Mockito.when(mockZoneManager.getResource(Mockito.any(Path.class))).thenReturn(mockResource);
        RoutineExecutor routineExecutor = new RoutineExecutor();
        routineExecutor.setWeCrossHost(mockWeCrossHost);
        routineExecutor.checkHtlcResources("a.b.c");
    }

    @Test
    public void initHTLCResourcePairsTest() throws Exception {
        HTLCTaskInfo htlcTaskInfo = new HTLCTaskInfo("a.b.c", "ac", "0x", "a.b.c", "ac", "0x");
        WeCrossHost mockWeCrossHost = Mockito.mock(WeCrossHost.class);
        RoutineManager mockRoutineManager = Mockito.mock(RoutineManager.class);
        HTLCManager mockHTLCManager = Mockito.mock(HTLCManager.class);
        AccountManager mockAccountManager = Mockito.mock(AccountManager.class);
        Account mockAccount = Mockito.mock(Account.class);
        ZoneManager mockZoneManager = Mockito.mock(ZoneManager.class);
        Resource mockResource = Mockito.mock(Resource.class);

        Mockito.when(mockWeCrossHost.getZoneManager()).thenReturn(mockZoneManager);
        Mockito.when(mockZoneManager.getResource(Mockito.any(Path.class))).thenReturn(mockResource);
        Mockito.when(mockWeCrossHost.getRoutineManager()).thenReturn(mockRoutineManager);
        Mockito.when(mockRoutineManager.getHtlcManager()).thenReturn(mockHTLCManager);
        Mockito.when(mockWeCrossHost.getAccountManager()).thenReturn(mockAccountManager);
        Mockito.when(mockAccountManager.getAccount("ac")).thenReturn(mockAccount);

        Map<String, HTLCTaskInfo> htlcTaskInfos = new HashMap<>();
        htlcTaskInfos.put("a.b.c", htlcTaskInfo);
        Mockito.when(mockHTLCManager.getHtlcTaskInfos()).thenReturn(htlcTaskInfos);

        RoutineExecutor routineExecutor = new RoutineExecutor();
        routineExecutor.setWeCrossHost(mockWeCrossHost);
        List<HTLCResourcePair> htlcResourcePairs = routineExecutor.initHTLCResourcePairs();
        Assert.assertEquals(htlcResourcePairs.size(), 1);
    }

    @Test
    public void startTest() {
        WeCrossHost mockWeCrossHost = Mockito.mock(WeCrossHost.class);
        RoutineManager mockRoutineManager = Mockito.mock(RoutineManager.class);
        HTLCManager mockHTLCManager = Mockito.mock(HTLCManager.class);
        Mockito.when(mockWeCrossHost.getRoutineManager()).thenReturn(mockRoutineManager);
        Mockito.when(mockRoutineManager.getHtlcManager()).thenReturn(mockHTLCManager);
        RoutineExecutor routineExecutor = new RoutineExecutor();
        routineExecutor.setWeCrossHost(mockWeCrossHost);
        routineExecutor.start();
    }
}
