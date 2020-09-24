package com.webank.wecross.routine.htlc;

import com.webank.wecross.polling.Task;
import com.webank.wecross.polling.TaskManager;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.routine.RoutineDefault;
import com.webank.wecross.stub.Path;
import com.webank.wecross.zone.ZoneManager;
import java.util.*;

public class HTLCManager {

    Map<String, HTLCContext> htlcTaskDataMap = new HashMap<>();
    HTLCResourcePair[] htlcResourcePairs;

    public void registerTask(TaskManager taskManager) {
        if (Objects.nonNull(htlcResourcePairs) && htlcResourcePairs.length > 0) {
            HTLCTaskFactory htlcTaskFactory = new HTLCTaskFactory();
            Task[] tasks =
                    htlcTaskFactory.load(
                            htlcResourcePairs, RoutineDefault.HTLC_JOB_DATA_KEY, HTLCJob.class);
            taskManager.registerTasks(tasks);
        }
    }

    public Resource filterHTLCResource(ZoneManager zoneManager, Path path, Resource resource) {
        if (htlcTaskDataMap.containsKey(path.toString())) {
            HTLCContext htlcContext = htlcTaskDataMap.get(path.toString());

            HTLCResource htlcResource =
                    new HTLCResource(zoneManager, path, htlcContext.getCounterpartyPath());
            htlcResource.setAdminUa(htlcContext.getAdminUA());
            htlcResource.setDriver(resource.getDriver());

            return htlcResource;
        }
        return resource;
    }

    public void initHTLCResourcePairs(ZoneManager zoneManager) {
        htlcResourcePairs = new HTLCResourcePair[htlcTaskDataMap.size()];
        int num = 0;
        for (HTLCContext htlcContext : htlcTaskDataMap.values()) {
            Path selfPath = htlcContext.getSelfPath();
            Path counterpartyPath = htlcContext.getCounterpartyPath();

            HTLCResource selfHTLCResource =
                    new HTLCResource(zoneManager, selfPath, counterpartyPath);
            selfHTLCResource.setAdminUa(htlcContext.getAdminUA());

            // no need to set counterpartyAddress
            HTLCResource counterpartyHTLCResource =
                    new HTLCResource(zoneManager, counterpartyPath, selfPath);
            counterpartyHTLCResource.setAdminUa(htlcContext.getAdminUA());

            HTLC weCrossHTLC = new HTLCImpl();
            htlcResourcePairs[num++] =
                    new HTLCResourcePair(weCrossHTLC, selfHTLCResource, counterpartyHTLCResource);
        }
    }

    public Map<String, HTLCContext> getHtlcTaskDataMap() {
        return htlcTaskDataMap;
    }

    public void setHtlcTaskDataMap(Map<String, HTLCContext> htlcTaskDataMap) {
        this.htlcTaskDataMap = htlcTaskDataMap;
    }

    public HTLCResourcePair[] getHtlcResourcePairs() {
        return htlcResourcePairs;
    }

    public void setHtlcResourcePairs(HTLCResourcePair[] htlcResourcePairs) {
        this.htlcResourcePairs = htlcResourcePairs;
    }
}
