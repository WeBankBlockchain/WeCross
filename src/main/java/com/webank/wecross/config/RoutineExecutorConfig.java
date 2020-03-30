package com.webank.wecross.config;

import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.routine.RoutineExecutor;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoutineExecutorConfig {
    @Resource private WeCrossHost weCrossHost;

    @Bean
    public RoutineExecutor newRoutineExecutor() {
        RoutineExecutor routineExecutor = new RoutineExecutor();
        routineExecutor.setWeCrossHost(weCrossHost);
        routineExecutor.start();
        return routineExecutor;
    }
}
