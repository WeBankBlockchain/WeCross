package com.webank.wecross.routine;

import com.webank.wecross.routine.htlc.HTLCManager;
import com.webank.wecross.routine.task.TaskManager;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoutineManager {
    private Logger logger = LoggerFactory.getLogger(RoutineManager.class);

    private HTLCManager htlcManager;
    private TaskManager taskManager;

    public void start() {
        /* register htlc task */
        if (htlcManager != null) {
            htlcManager.registerTask(taskManager);
        }

        /* start all routine tasks */
        try {
            taskManager.start();
        } catch (SchedulerException e) {
            logger.error("Failed to start TaskManager: {}", e.getMessage(), e);
        }
    }

    public HTLCManager getHtlcManager() {
        return htlcManager;
    }

    public void setHtlcManager(HTLCManager htlcManager) {
        this.htlcManager = htlcManager;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public void setTaskManager(TaskManager taskManager) {
        this.taskManager = taskManager;
    }
}
