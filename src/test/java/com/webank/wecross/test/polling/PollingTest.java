package com.webank.wecross.test.polling;

import com.webank.wecross.interchain.InterchainManager;
import com.webank.wecross.polling.PollingManager;
import com.webank.wecross.polling.TaskManager;
import com.webank.wecross.routine.RoutineManager;
import org.junit.Test;
import org.mockito.Mockito;

public class PollingTest {
    @Test
    public void pollingManagerTest() {
        PollingManager pollingManager = new PollingManager();
        pollingManager.setInterchainManager(mockInterchainManager());
        pollingManager.setRoutineManager(mockRoutineManager());
        pollingManager.polling();
    }

    private InterchainManager mockInterchainManager() {
        InterchainManager interchainManager = Mockito.mock(InterchainManager.class);
        Mockito.doNothing().when(interchainManager).registerTask(Mockito.isA(TaskManager.class));

        return interchainManager;
    }

    private RoutineManager mockRoutineManager() {
        RoutineManager routineManager = Mockito.mock(RoutineManager.class);
        Mockito.doNothing().when(routineManager).registerTask(Mockito.isA(TaskManager.class));

        return routineManager;
    }
}
