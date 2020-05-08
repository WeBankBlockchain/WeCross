package com.webank.wecross.config;

import com.webank.wecross.routine.RoutineManager;
import com.webank.wecross.routine.htlc.HTLCManager;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoutineManagerConfig {
    @Resource HTLCManager htlcManager;

    @Bean
    public RoutineManager newRoutineManager() {
        System.out.println("Initializing RoutineManager ...");

        RoutineManager routineManager = new RoutineManager();
        routineManager.setHtlcManager(htlcManager);
        return routineManager;
    }
}
