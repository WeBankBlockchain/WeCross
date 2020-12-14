package com.webank.wecross.interchain;

import com.webank.wecross.polling.Task;
import com.webank.wecross.polling.TaskFactory;
import com.webank.wecross.routine.RoutineDefault;
import org.quartz.*;

public class InterchainTaskFactory implements TaskFactory {
    @Override
    public <T extends Job> Task[] load(Object[] contexts, String dataKey, Class<T> jobType) {
        Task[] tasks = new Task[contexts.length];
        int num = 0;
        for (Object context : contexts) {
            SystemResource systemResource = (SystemResource) context;
            String path = systemResource.getHubResource().getPath().toString();
            String jobName = "inter_chain_job_" + path;
            String triggerName = "inter_chain_trigger_" + path;
            JobDetail jobDetail =
                    loadInterchainJobDetail(jobName, dataKey, jobType, systemResource);

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

    private <T extends Job> JobDetail loadInterchainJobDetail(
            String jobName, String dataKey, Class<T> jobType, SystemResource systemResource) {
        // create job
        JobDetail jobDetail =
                JobBuilder.newJob(jobType).withIdentity(jobName, Scheduler.DEFAULT_GROUP).build();

        // send args to job context
        jobDetail.getJobDataMap().putIfAbsent(dataKey, systemResource);
        return jobDetail;
    }
}
