package com.webank.wecross.test.routine;

import com.webank.wecross.resource.Resource;
import com.webank.wecross.routine.htlc.HTLCManager;
import com.webank.wecross.routine.htlc.HTLCResource;
import com.webank.wecross.routine.htlc.HTLCTaskData;
import com.webank.wecross.stub.Path;
import com.webank.wecross.zone.ZoneManager;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class HTLCManagerTest {
    @Test
    public void filterHTLCResourceTest() throws Exception {
        ZoneManager mockZoneManager = Mockito.mock(ZoneManager.class);
        Path mockPath = Mockito.mock(Path.class);
        Resource mockResource = Mockito.mock(Resource.class);

        Mockito.when(mockPath.toString()).thenReturn("a.b.c");
        HTLCManager htlcManager = new HTLCManager();
        Assert.assertEquals(
                mockResource,
                htlcManager.filterHTLCResource(mockZoneManager, mockPath, mockResource));

        HTLCTaskData htlcTaskData = new HTLCTaskData();
        htlcTaskData.setSelfPath(Path.decode("a.b.c"));
        Map<String, HTLCTaskData> htlcTaskDataMap = new HashMap<>();
        htlcTaskDataMap.put("a.b.c", htlcTaskData);
        htlcManager.setHtlcTaskDataMap(htlcTaskDataMap);
        Mockito.when(mockZoneManager.getResource(Mockito.any(Path.class))).thenReturn(mockResource);
        Assert.assertEquals(
                HTLCResource.class,
                htlcManager.filterHTLCResource(mockZoneManager, mockPath, mockResource).getClass());
    }

    @Test
    public void initHTLCResourcePairsTest() throws Exception {
        HTLCTaskData htlcTaskData = new HTLCTaskData();
        htlcTaskData.setSelfPath(Path.decode("a.b.c"));
        Map<String, HTLCTaskData> htlcTaskDataMap = new HashMap<>();
        htlcTaskDataMap.put("a.b.c", htlcTaskData);
        HTLCManager htlcManager = new HTLCManager();
        htlcManager.setHtlcTaskDataMap(htlcTaskDataMap);
        htlcManager.initHTLCResourcePairs(null);
        Assert.assertEquals(1, htlcManager.getHtlcResourcePairs().length);
    }
}
