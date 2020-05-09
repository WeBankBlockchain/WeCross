package com.webank.wecross.routine.htlc;

import com.webank.wecross.routine.task.Task;
import com.webank.wecross.routine.task.TaskFactory;
import java.util.ArrayList;
import java.util.List;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTLCTaskFactory implements TaskFactory {
    private Logger logger = LoggerFactory.getLogger(HTLCTaskFactory.class);

    @Override
    public List<Task> load(Object... args) {
        @SuppressWarnings("unchecked")
        List<HTLCResourcePair> htlcResourcePairs = (List<HTLCResourcePair>) args[0];
        List<Task> tasks = new ArrayList<>();
        int num = htlcResourcePairs.size();
        for (int i = 0; i < num; i++) {
            HTLCResourcePair htlcResourcePair = htlcResourcePairs.get(i);
            logger.debug("HTLCResourcePair: {}", htlcResourcePair.toString());

            String jobName = htlcResourcePair.getSelfHTLCResource().getSelfPath().toString();
            JobDetail jobDetail = loadHTLCJobDetail(jobName, "HTLC", htlcResourcePair);

            // execute per 2 seconds
            Trigger trigger =
                    TriggerBuilder.newTrigger()
                            .withIdentity(jobName, Scheduler.DEFAULT_GROUP)
                            .withSchedule(
                                    SimpleScheduleBuilder.simpleSchedule()
                                            .withIntervalInSeconds(2)
                                            .repeatForever())
                            .build();
            tasks.add(new Task(trigger, jobDetail));
        }
        return tasks;
    }

    public JobDetail loadHTLCJobDetail(
            String jobName, String dataKey, HTLCResourcePair htlcResourcePair) {
        // create job
        JobDetail jobDetail =
                JobBuilder.newJob(HTLCJob.class)
                        .withIdentity(jobName, Scheduler.DEFAULT_GROUP)
                        .build();

        // send args to job context
        jobDetail.getJobDataMap().putIfAbsent(dataKey, htlcResourcePair);
        return jobDetail;
    }
}
