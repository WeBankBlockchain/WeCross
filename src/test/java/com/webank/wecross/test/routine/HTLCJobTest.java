package com.webank.wecross.test.routine;

import com.webank.wecross.resource.Resource;
import com.webank.wecross.routine.htlc.AssetHTLC;
import com.webank.wecross.routine.htlc.HTLCJob;
import com.webank.wecross.routine.htlc.HTLCResource;
import com.webank.wecross.routine.htlc.HTLCResourcePair;
import com.webank.wecross.routine.htlc.HTLCTaskFactory;
import org.junit.Test;
import org.mockito.Mockito;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

public class HTLCJobTest {
    @Test
    public void executeTest() throws Exception {
        JobExecutionContext mockNetworkManager = Mockito.mock(JobExecutionContext.class);
        HTLCJob htlcJob = new HTLCJob();
        HTLCTaskFactory htlcTaskFactory = new HTLCTaskFactory();
        JobDetail jobDetail =
                htlcTaskFactory.loadHTLCJobDetail("HTLC", "HTLC", getHTLCResourcePair());
        Mockito.when(mockNetworkManager.getJobDetail()).thenReturn(jobDetail);
        htlcJob.execute(mockNetworkManager);
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

    private HTLCResourcePair getHTLCResourcePair() throws Exception {
        Resource resource = new Resource();
        HTLCResource assetHTLCResource1 = new HTLCResource(resource);
        HTLCResource assetHTLCResource2 = new HTLCResource(resource);
        AssetHTLC assetHTLC = new AssetHTLC();
        return new HTLCResourcePair(assetHTLC, assetHTLCResource1, assetHTLCResource2);
    }
}
