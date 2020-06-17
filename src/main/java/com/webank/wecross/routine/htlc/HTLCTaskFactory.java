package com.webank.wecross.routine.htlc;

import com.webank.wecross.routine.RoutineDefault;
import com.webank.wecross.routine.task.Task;
import com.webank.wecross.routine.task.TaskFactory;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

public class HTLCTaskFactory implements TaskFactory {

    @Override
    public Task[] load(Object[] contexts) {
        Task[] tasks = new Task[contexts.length];
        int num = 0;
        for (Object context : contexts) {
            HTLCResourcePair htlcResourcePair = (HTLCResourcePair) context;
            String jobName = htlcResourcePair.getSelfHTLCResource().getSelfPath().toString();
            JobDetail jobDetail = loadHtlcJobDetail(jobName, htlcResourcePair);

            // execute per 1 seconds
            Trigger trigger =
                    TriggerBuilder.newTrigger()
                            .withIdentity(jobName, Scheduler.DEFAULT_GROUP)
                            .withSchedule(
                                    SimpleScheduleBuilder.simpleSchedule()
                                            .withIntervalInMilliseconds(
                                                    RoutineDefault.POLLING_INTERVAL)
                                            .repeatForever())
                            .build();
            tasks[num++] = new Task(trigger, jobDetail);
        }
        return tasks;
    }

    private JobDetail loadHtlcJobDetail(String jobName, HTLCResourcePair htlcResourcePair) {
        // create job
        JobDetail jobDetail =
                JobBuilder.newJob(HTLCJob.class)
                        .withIdentity(jobName, Scheduler.DEFAULT_GROUP)
                        .build();

        // send args to job context
        jobDetail.getJobDataMap().putIfAbsent(RoutineDefault.HTLC_JOB_DATA_KEY, htlcResourcePair);
        return jobDetail;
    }
}
