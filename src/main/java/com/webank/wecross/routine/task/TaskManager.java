package com.webank.wecross.routine.task;

import java.util.List;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskManager {

    private Logger logger = LoggerFactory.getLogger(TaskManager.class);

    private Scheduler scheduler;
    private TaskFactory taskFactory;

    public TaskManager(TaskFactory taskFactory) {
        this.taskFactory = taskFactory;
        try {
            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            scheduler = schedulerFactory.getScheduler();
        } catch (SchedulerException e) {
            logger.error("something wrong with getting scheduler: {}", e.getLocalizedMessage());
        }
    }

    public void registerTasks(Object... args) {
        List<Task> tasks = taskFactory.load(args);
        try {
            for (Task task : tasks) {
                scheduler.scheduleJob(task.getJobDetail(), task.getTrigger());
            }
        } catch (SchedulerException e) {
            logger.error("something wrong with registering tasks: {}", e.getLocalizedMessage());
        }
    }

    public void start() {
        try {
            logger.info("scheduler starts working");
            scheduler.start();
        } catch (SchedulerException e) {
            logger.error("something wrong with starting scheduler");
        }
    }
}
