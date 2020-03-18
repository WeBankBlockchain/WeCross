package com.webank.wecross.routine.htlc;

import com.webank.wecross.host.WeCrossHost;
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
        String path = htlcResourcePair.getSelfHTLCResource().getPath();
        try {
            doHTLCTask(htlcResourcePair);
        } catch (Exception e) {
            logger.error(
                    "error in current round, path: {}, errorMessage: {}",
                    path,
                    e.getLocalizedMessage());
        }
    }

    public void doHTLCTask(HTLCResourcePair htlcResourcePair) throws Exception {
        HTLC htlc = htlcResourcePair.getHtlc();
        HTLCScheduler htlcScheduler = new HTLCScheduler(htlc);
        // get unfinished htlc task
        String h = htlcScheduler.getTask(htlcResourcePair.getSelfHTLCResource());
        if (!h.equalsIgnoreCase("null")) {
            logger.info("start running htlc task: {}", h);
            htlcScheduler.start(htlcResourcePair, h);
        } else {
            logger.info("no unfinished htlc task, continue listening");
        }
    }
}
