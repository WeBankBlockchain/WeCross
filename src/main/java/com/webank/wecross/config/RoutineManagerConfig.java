package com.webank.wecross.config;

import com.webank.wecross.routine.RoutineManager;
import com.webank.wecross.routine.htlc.HTLCManager;
import com.webank.wecross.routine.task.TaskManager;
import javax.annotation.Resource;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoutineManagerConfig {
    private Logger logger = LoggerFactory.getLogger(RoutineManagerConfig.class);

    @Resource HTLCManager htlcManager;

    @Bean
    public RoutineManager newRoutineManager() {
        System.out.println("Initializing RoutineManager ...");

        RoutineManager routineManager = new RoutineManager();
        routineManager.setHtlcManager(htlcManager);

        TaskManager taskManager = new TaskManager();
        try {
            taskManager.init();
        } catch (SchedulerException e) {
            logger.error("Failed to init TaskManager: {}", e.getMessage(), e);
            System.exit(1);
        }
        routineManager.setTaskManager(taskManager);

        routineManager.start();
        return routineManager;
    }
}
