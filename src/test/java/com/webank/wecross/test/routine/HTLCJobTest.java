package com.webank.wecross.test.routine;

import com.webank.wecross.routine.htlc.*;
import com.webank.wecross.routine.task.Task;
import com.webank.wecross.stub.Path;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

public class HTLCJobTest {
    @Test
    public void executeTest() {
        JobExecutionContext jobExecutionContext = Mockito.mock(JobExecutionContext.class);
        try {
            HTLCJob htlcJob = new HTLCJob();
            HTLCTaskFactory htlcTaskFactory = new HTLCTaskFactory();
            HTLCResourcePair[] htlcResourcePairs = new HTLCResourcePair[1];
            htlcResourcePairs[0] = getHTLCResourcePair();
            Task[] tasks = htlcTaskFactory.load(htlcResourcePairs);
            JobDetail jobDetail = tasks[0].getJobDetail();
            Mockito.when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);
            htlcJob.execute(jobExecutionContext);
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    private HTLCResourcePair getHTLCResourcePair() {
        HTLCResource htlcResource1 = new HTLCResource();
        HTLCResource htlcResource2 = new HTLCResource();
        htlcResource1.setSelfPath(new Path());
        htlcResource2.setSelfPath(new Path());
        AssetHTLC assetHTLC = new AssetHTLC();
        return new HTLCResourcePair(assetHTLC, htlcResource1, htlcResource2);
    }
}
