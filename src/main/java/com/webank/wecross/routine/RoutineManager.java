package com.webank.wecross.routine;

import com.webank.wecross.polling.TaskManager;
import com.webank.wecross.routine.htlc.HTLCManager;
import com.webank.wecross.routine.xa.XATransactionManager;
import java.util.Objects;

public class RoutineManager {

    private HTLCManager htlcManager;
    private XATransactionManager xaTransactionManager;

    public void registerTask(TaskManager taskManager) {
        /* register htlc task */
        if (Objects.nonNull(htlcManager)) {
            htlcManager.registerTask(taskManager);
        }

        /* register xa task */
        if (Objects.nonNull(xaTransactionManager)) {
            xaTransactionManager.registerTask(taskManager);
        }
    }

    public HTLCManager getHtlcManager() {
        return htlcManager;
    }

    public void setHtlcManager(HTLCManager htlcManager) {
        this.htlcManager = htlcManager;
    }

    public XATransactionManager getXaTransactionManager() {
        return xaTransactionManager;
    }

    public void setXaTransactionManager(XATransactionManager xaTransactionManager) {
        this.xaTransactionManager = xaTransactionManager;
    }
}
