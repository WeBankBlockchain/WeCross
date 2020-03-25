package com.webank.wecross.routine;

import com.webank.wecross.routine.htlc.HTLCManager;

public class RoutineManager {
    private HTLCManager htlcManager;

    public HTLCManager getHtlcManager() {
        return htlcManager;
    }

    public void setHtlcManager(HTLCManager htlcManager) {
        this.htlcManager = htlcManager;
    }
}
