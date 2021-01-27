package com.webank.wecross.polling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskManager {

    private Logger logger = LoggerFactory.getLogger(TaskManager.class);

    private Scheduler scheduler;
    private List<Task> tasks = new ArrayList<>();
    private static final int DEFAULT_THREAD_COUNT = 32;

    public void start() throws SchedulerException {
        /* init scheduler */
        int threadCount =
                tasks.isEmpty() ? DEFAULT_THREAD_COUNT : tasks.size() % DEFAULT_THREAD_COUNT;
        Properties props = new Properties();
        props.put(StdSchedulerFactory.PROP_THREAD_POOL_CLASS, "org.quartz.simpl.SimpleThreadPool");
        props.put("org.quartz.threadPool.threadCount", String.valueOf(threadCount));
        StdSchedulerFactory stdSchedulerFactory = new StdSchedulerFactory();
        stdSchedulerFactory.initialize(props);
        scheduler = stdSchedulerFactory.getScheduler();

        /* schedule current registered tasks*/
        scheduleTasks();

        logger.info("Polling scheduler starts working");
        scheduler.start();
    }

    public void registerTasks(Task[] tasks) {
        this.tasks.addAll(Arrays.asList(tasks));
    }

    /** only used when scheduler has started */
    public void appendTasks(Task[] tasks) throws SchedulerException {
        if (Objects.isNull(scheduler)) {
            throw new SchedulerException("Scheduler hasn't been initialized yet");
        }

        this.tasks.addAll(Arrays.asList(tasks));
        for (Task task : tasks) {
            scheduler.scheduleJob(task.getJobDetail(), task.getTrigger());
        }
    }

    private void scheduleTasks() throws SchedulerException {
        for (Task task : tasks) {
            scheduler.scheduleJob(task.getJobDetail(), task.getTrigger());
        }
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
}
