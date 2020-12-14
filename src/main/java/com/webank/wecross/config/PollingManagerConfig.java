package com.webank.wecross.config;

import com.webank.wecross.interchain.InterchainManager;
import com.webank.wecross.polling.PollingManager;
import com.webank.wecross.routine.RoutineManager;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PollingManagerConfig {
    @Resource RoutineManager routineManager;

    @Resource InterchainManager interchainManager;

    @Bean
    public PollingManager newPollingManager() {
        System.out.println("Initializing PollingManager ...");

        PollingManager pollingManager = new PollingManager();
        pollingManager.setRoutineManager(routineManager);
        pollingManager.setInterchainManager(interchainManager);

        pollingManager.polling();
        return pollingManager;
    }
}
