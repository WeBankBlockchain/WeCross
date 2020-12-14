package com.webank.wecross.polling;

import com.webank.wecross.interchain.InterchainManager;
import com.webank.wecross.routine.RoutineManager;
import java.util.Objects;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollingManager {
    private Logger logger = LoggerFactory.getLogger(PollingManager.class);

    private TaskManager taskManager = new TaskManager();
    private RoutineManager routineManager;
    private InterchainManager interchainManager;

    public void polling() {
        /* register routine task */
        if (Objects.nonNull(routineManager)) {
            routineManager.registerTask(taskManager);
        }

        /* register interchain task */
        if (Objects.nonNull(interchainManager)) {
            interchainManager.registerTask(taskManager);
        }

        /* start polling tasks */
        try {
            taskManager.start();
        } catch (SchedulerException e) {
            logger.error("Failed to start polling scheduler: {}", e.getMessage(), e);
        }
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public void setTaskManager(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public RoutineManager getRoutineManager() {
        return routineManager;
    }

    public void setRoutineManager(RoutineManager routineManager) {
        this.routineManager = routineManager;
    }

    public InterchainManager getInterchainManager() {
        return interchainManager;
    }

    public void setInterchainManager(InterchainManager interchainManager) {
        this.interchainManager = interchainManager;
    }
}
