package com.webank.wecross.routine;

import com.webank.wecross.account.AccountManager;
import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.routine.htlc.HTLC;
import com.webank.wecross.routine.htlc.HTLCResource;
import com.webank.wecross.routine.htlc.HTLCResourcePair;
import com.webank.wecross.routine.htlc.HTLCTaskFactory;
import com.webank.wecross.routine.htlc.HTLCTaskInfo;
import com.webank.wecross.routine.htlc.WeCrossHTLC;
import com.webank.wecross.routine.task.TaskManager;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Path;
import com.webank.wecross.zone.ZoneManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoutineExecutor {
    private Logger logger = LoggerFactory.getLogger(RoutineExecutor.class);

    private WeCrossHost weCrossHost;

    public WeCrossHost getWeCrossHost() {
        return weCrossHost;
    }

    public void setWeCrossHost(WeCrossHost weCrossHost) {
        this.weCrossHost = weCrossHost;
    }

    public void start() {
        /* start htlc service */
        runHTLC();
    }

    public void runHTLC() {
        try {
            if (weCrossHost.getRoutineManager().getHtlcManager() != null) {
                HTLCTaskFactory htlcTaskFactory = new HTLCTaskFactory();
                TaskManager taskManager = new TaskManager(htlcTaskFactory);
                taskManager.registerTasks(initHTLCResourcePairs());
                taskManager.start();
            }
        } catch (Exception e) {
            logger.error(
                    "something wrong with runHTLCService: {}, exception: {}", e.getMessage(), e);
        }
    }

    public List<HTLCResourcePair> initHTLCResourcePairs() throws Exception {
        List<HTLCResourcePair> htlcResourcePairs = new ArrayList<>();
        Map<String, HTLCTaskInfo> htlcTaskInfos =
                weCrossHost.getRoutineManager().getHtlcManager().getHtlcTaskInfos();
        for (HTLCTaskInfo htlcTaskInfo : htlcTaskInfos.values()) {
            String selfPath = htlcTaskInfo.getSelfPath();
            String counterpartyPath = htlcTaskInfo.getCounterpartyPath();
            checkHtlcResources(selfPath);

            AccountManager accountManager = weCrossHost.getAccountManager();
            HTLCResource selfHTLCResource =
                    new HTLCResource(
                            weCrossHost, Path.decode(selfPath), Path.decode(counterpartyPath));
            selfHTLCResource.setCounterpartyAddress(htlcTaskInfo.getCounterpartyAddress());
            Account selfAccount = accountManager.getAccount(htlcTaskInfo.getSelfAccount());
            selfHTLCResource.setAccount(selfAccount);

            // no need to set counterpartyAddress
            HTLCResource counterpartyHTLCResource =
                    new HTLCResource(
                            weCrossHost, Path.decode(counterpartyPath), Path.decode(selfPath));
            // counterpartyHTLCResource.setCounterpartyAddress(htlcTaskInfo.getSelfAddress());
            Account counterpartyAccount =
                    accountManager.getAccount(htlcTaskInfo.getCounterpartyAccount());
            counterpartyHTLCResource.setAccount(counterpartyAccount);

            HTLC weCrossHTLC = new WeCrossHTLC();
            htlcResourcePairs.add(
                    new HTLCResourcePair(weCrossHTLC, selfHTLCResource, counterpartyHTLCResource));
        }
        return htlcResourcePairs;
    }

    public void checkHtlcResources(String selfPath) throws Exception {
        ZoneManager zoneManager = weCrossHost.getZoneManager();
        Resource selfResource = zoneManager.getResource(Path.decode(selfPath));
        if (selfResource == null) {
            throw new Exception("htlc resource: " + selfPath + " not found");
        }
    }
}
