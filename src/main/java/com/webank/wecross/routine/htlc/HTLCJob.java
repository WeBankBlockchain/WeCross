package com.webank.wecross.routine.htlc;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.routine.RoutineDefault;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTLCJob implements Job {

    private Logger logger = LoggerFactory.getLogger(WeCrossHost.class);

    public void execute(JobExecutionContext context) {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        HTLCResourcePair htlcResourcePair = (HTLCResourcePair) dataMap.get("HTLC");
        String path = htlcResourcePair.getSelfHTLCResource().getSelfPath().toString();
        try {
            doHTLCTask(htlcResourcePair);
        } catch (WeCrossException e) {
            logger.error(
                    "current round failed, path: {}, errorMessage: {}, internalMessage: {}",
                    path,
                    e.getLocalizedMessage(),
                    e.getInternalMessage());
        }
    }

    public void doHTLCTask(HTLCResourcePair htlcResourcePair) throws WeCrossException {
        HTLC htlc = htlcResourcePair.getHtlc();
        HTLCScheduler htlcScheduler = new HTLCScheduler(htlc);
        // get unfinished htlc task
        HTLCResource htlcResource = htlcResourcePair.getSelfHTLCResource();
        String h = htlcScheduler.getTask(htlcResource);
        if (!h.equalsIgnoreCase(RoutineDefault.NULL_FLAG)) {
            logger.info("start running htlc task: {}, path; {}", h, htlcResource.getSelfPath());
            htlcScheduler.start(htlcResourcePair, h);
        }
    }
}
