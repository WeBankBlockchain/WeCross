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

public class HTLCTaskFactory implements TaskFactory {

    public List<Task> load(Object... args) {
        @SuppressWarnings("unchecked")
        List<HTLCResourcePair> htlcResourcePairs = (List<HTLCResourcePair>) args[0];
        List<Task> tasks = new ArrayList<>();
        int num = htlcResourcePairs.size();
        for (int i = 0; i < num; i++) {
            HTLCResourcePair htlcResourcePair = htlcResourcePairs.get(i);
            String jobName = htlcResourcePair.getSelfHTLCResource().getPath();
            JobDetail jobDetail = loadHTLCJobDetail(jobName, "HTLC", htlcResourcePair);

            // execute per 5 seconds
            Trigger trigger =
                    TriggerBuilder.newTrigger()
                            .withIdentity(jobName, Scheduler.DEFAULT_GROUP)
                            .withSchedule(
                                    SimpleScheduleBuilder.simpleSchedule()
                                            .withIntervalInSeconds(5)
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
