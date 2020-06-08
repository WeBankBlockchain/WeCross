package com.webank.wecross.routine.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    private static final int DEFAULT_THREAD_COUNT = 16;

    public void init() throws SchedulerException {
        int threadCount =
                tasks.isEmpty() ? DEFAULT_THREAD_COUNT : tasks.size() % DEFAULT_THREAD_COUNT;
        StdSchedulerFactory stdSchedulerFactory = new StdSchedulerFactory();
        Properties props = new Properties();
        props.put(StdSchedulerFactory.PROP_THREAD_POOL_CLASS, "org.quartz.simpl.SimpleThreadPool");
        props.put("org.quartz.threadPool.threadCount", String.valueOf(threadCount));
        stdSchedulerFactory.initialize(props);
        scheduler = stdSchedulerFactory.getScheduler();
    }

    public void start() throws SchedulerException {
        logger.info("Scheduler starts working");
        scheduler.start();
    }

    public void addTasks(Task[] tasks) throws SchedulerException {
        this.tasks.addAll(Arrays.asList(tasks));
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
}
