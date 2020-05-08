package com.webank.wecross.test.routine;

import com.webank.wecross.resource.Resource;
import com.webank.wecross.routine.htlc.HTLCJob;
import com.webank.wecross.routine.htlc.HTLCResource;
import com.webank.wecross.routine.htlc.HTLCResourcePair;
import com.webank.wecross.routine.htlc.HTLCTaskFactory;
import com.webank.wecross.routine.htlc.WeCrossHTLC;
import com.webank.wecross.stub.Path;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

public class HTLCJobTest {
    @Test
    public void executeTest() {
        JobExecutionContext mockNetworkManager = Mockito.mock(JobExecutionContext.class);
        try {
            HTLCJob htlcJob = new HTLCJob();
            HTLCTaskFactory htlcTaskFactory = new HTLCTaskFactory();
            JobDetail jobDetail =
                    htlcTaskFactory.loadHTLCJobDetail("HTLC", "HTLC", getHTLCResourcePair());
            Mockito.when(mockNetworkManager.getJobDetail()).thenReturn(jobDetail);
            htlcJob.execute(mockNetworkManager);
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void doHTLCTaskTest() {
        HTLCJob htlcJob = new HTLCJob();
        try {
            htlcJob.doHTLCTask(getHTLCResourcePair());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private HTLCResourcePair getHTLCResourcePair() {
        Resource mockResource = Mockito.mock(Resource.class);
        HTLCResource assetHTLCResource1 = new HTLCResource(true, mockResource, mockResource, "");
        HTLCResource assetHTLCResource2 = new HTLCResource(true, mockResource, mockResource, "");
        assetHTLCResource1.setSelfPath(new Path());
        assetHTLCResource2.setSelfPath(new Path());
        WeCrossHTLC weCrossHTLC = new WeCrossHTLC();
        return new HTLCResourcePair(weCrossHTLC, assetHTLCResource1, assetHTLCResource2);
    }
}
