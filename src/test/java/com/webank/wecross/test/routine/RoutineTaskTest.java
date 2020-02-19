package com.webank.wecross.test.routine;

import com.webank.wecross.resource.Path;
import com.webank.wecross.routine.htlc.AssetHTLC;
import com.webank.wecross.routine.htlc.AssetHTLCResource;
import com.webank.wecross.routine.htlc.HTLCResourcePair;
import com.webank.wecross.routine.htlc.HTLCTaskFactory;
import com.webank.wecross.routine.task.Task;
import com.webank.wecross.routine.task.TaskFactory;
import com.webank.wecross.routine.task.TaskManager;
import com.webank.wecross.stub.bcos.BCOSContractResource;
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
            String path1 = htlcResourcePair.getSelfHTLCResource().getPathAsString();
            String path2 = getHTLCResourcePairs().get(i).getSelfHTLCResource().getPathAsString();
            Assert.assertEquals(path1, path2);
            String path3 = htlcResourcePair.getCounterpartyHTLCResource().getPathAsString();
            String path4 =
                    getHTLCResourcePairs().get(i).getCounterpartyHTLCResource().getPathAsString();
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
        BCOSContractResource bcosContractResource1 = new BCOSContractResource();
        bcosContractResource1.setPath(Path.decode("payment.bcos.test1"));
        BCOSContractResource bcosContractResource2 = new BCOSContractResource();
        bcosContractResource2.setPath(Path.decode("payment.bcos.test2"));
        AssetHTLCResource assetHTLCResource1 = new AssetHTLCResource(bcosContractResource1);
        AssetHTLCResource assetHTLCResource2 = new AssetHTLCResource(bcosContractResource2);
        AssetHTLC assetHTLC = new AssetHTLC();
        HTLCResourcePair htlcResourcePair1 =
                new HTLCResourcePair(assetHTLC, assetHTLCResource1, assetHTLCResource2);
        HTLCResourcePair htlcResourcePair2 =
                new HTLCResourcePair(assetHTLC, assetHTLCResource2, assetHTLCResource1);
        List<HTLCResourcePair> htlcResourcePairs = new ArrayList<>();
        htlcResourcePairs.add(htlcResourcePair1);
        htlcResourcePairs.add(htlcResourcePair2);
        return htlcResourcePairs;
    }
}
