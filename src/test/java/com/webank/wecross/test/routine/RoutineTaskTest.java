package com.webank.wecross.test.routine;

import com.webank.wecross.resource.Resource;
import com.webank.wecross.routine.htlc.AssetHTLC;
import com.webank.wecross.routine.htlc.HTLCResource;
import com.webank.wecross.routine.htlc.HTLCResourcePair;
import com.webank.wecross.routine.htlc.HTLCTaskFactory;
import com.webank.wecross.routine.task.Task;
import com.webank.wecross.routine.task.TaskFactory;
import com.webank.wecross.routine.task.TaskManager;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.quartz.JobDataMap;

public class RoutineTaskTest {
    @Test
    public void HTLCTaskFactoryTest() throws Exception {
        TaskFactory taskFactory = new HTLCTaskFactory();
        List<Task> tasks = taskFactory.load(getHTLCResourcePairs());
        Assert.assertEquals(2, tasks.size());
        for (int i = 0; i < 2; i++) {
            JobDataMap dataMap = tasks.get(i).getJobDetail().getJobDataMap();
            HTLCResourcePair htlcResourcePair = (HTLCResourcePair) dataMap.get("HTLC");
            String path1 = htlcResourcePair.getSelfHTLCResource().getPath();
            String path2 = getHTLCResourcePairs().get(i).getSelfHTLCResource().getPath();
            Assert.assertEquals(path1, path2);
            String path3 = htlcResourcePair.getCounterpartyHTLCResource().getPath();
            String path4 = getHTLCResourcePairs().get(i).getCounterpartyHTLCResource().getPath();
            Assert.assertEquals(path3, path4);
        }
    }

    @Test
    public void taskManagetTest() throws Exception {
        TaskFactory taskFactory = new HTLCTaskFactory();
        TaskManager taskManager = new TaskManager(taskFactory);
        taskManager.registerTasks(getHTLCResourcePairs());
    }

    private List<HTLCResourcePair> getHTLCResourcePairs() throws Exception {
        Resource resource = new Resource();
        HTLCResource htlcResource1 = new HTLCResource(resource);
        htlcResource1.setPath("a1.b1.c1");
        HTLCResource htlcResource2 = new HTLCResource(resource);
        htlcResource2.setPath("a2.b2.c2");
        AssetHTLC assetHTLC = new AssetHTLC();
        HTLCResourcePair htlcResourcePair1 =
                new HTLCResourcePair(assetHTLC, htlcResource1, htlcResource2);
        HTLCResourcePair htlcResourcePair2 =
                new HTLCResourcePair(assetHTLC, htlcResource2, htlcResource1);
        List<HTLCResourcePair> htlcResourcePairs = new ArrayList<>();
        htlcResourcePairs.add(htlcResourcePair1);
        htlcResourcePairs.add(htlcResourcePair2);
        return htlcResourcePairs;
    }
}
