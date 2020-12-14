package com.webank.wecross.routine.htlc;

import com.webank.wecross.polling.Task;
import com.webank.wecross.polling.TaskFactory;
import com.webank.wecross.routine.RoutineDefault;
import org.quartz.*;

public class HTLCTaskFactory implements TaskFactory {

    @Override
    public <T extends Job> Task[] load(Object[] contexts, String dataKey, Class<T> jobType) {
        Task[] tasks = new Task[contexts.length];
        int num = 0;
        for (Object context : contexts) {
            HTLCResourcePair htlcResourcePair = (HTLCResourcePair) context;
            String path = htlcResourcePair.getSelfHTLCResource().getSelfPath().toString();
            String jobName = "htlc_job_" + path;
            String triggerName = "htlc_trigger_" + path;
            JobDetail jobDetail = loadHtlcJobDetail(jobName, dataKey, jobType, htlcResourcePair);

            // execute per 1 seconds
            Trigger trigger =
                    TriggerBuilder.newTrigger()
                            .withIdentity(triggerName, Scheduler.DEFAULT_GROUP)
                            .withSchedule(
                                    SimpleScheduleBuilder.simpleSchedule()
                                            .withIntervalInMilliseconds(
                                                    RoutineDefault.POLLING_CYCLE)
                                            .repeatForever())
                            .build();
            tasks[num++] = new Task(trigger, jobDetail);
        }
        return tasks;
    }

    private <T extends Job> JobDetail loadHtlcJobDetail(
            String jobName, String dataKey, Class<T> jobType, HTLCResourcePair htlcResourcePair) {
        // create job
        JobDetail jobDetail =
                JobBuilder.newJob(jobType).withIdentity(jobName, Scheduler.DEFAULT_GROUP).build();

        // send args to job context
        jobDetail.getJobDataMap().putIfAbsent(dataKey, htlcResourcePair);
        return jobDetail;
    }
}
