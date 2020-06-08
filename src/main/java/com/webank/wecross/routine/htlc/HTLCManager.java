package com.webank.wecross.routine.htlc;

import com.webank.wecross.resource.Resource;
import com.webank.wecross.routine.task.Task;
import com.webank.wecross.routine.task.TaskManager;
import com.webank.wecross.stub.Path;
import com.webank.wecross.zone.ZoneManager;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTLCManager {
    private Logger logger = LoggerFactory.getLogger(HTLCManager.class);

    Map<String, HTLCTaskData> htlcTaskDataMap = new HashMap<>();
    HTLCResourcePair[] htlcResourcePairs;

    public void registerTask(TaskManager taskManager) {
        if (htlcResourcePairs != null) {
            try {
                HTLCTaskFactory htlcTaskFactory = new HTLCTaskFactory();
                Task[] tasks = htlcTaskFactory.load(htlcResourcePairs);
                taskManager.addTasks(tasks);
            } catch (Exception e) {
                logger.error("Failed to add htlc tasks: {}", e.getMessage(), e);
            }
        }
    }

    public Resource filterHTLCResource(ZoneManager zoneManager, Path path, Resource resource) {
        if (htlcTaskDataMap.containsKey(path.toString())) {
            HTLCTaskData htlcTaskData = htlcTaskDataMap.get(path.toString());

            HTLCResource htlcResource =
                    new HTLCResource(zoneManager, path, htlcTaskData.getCounterpartyPath());
            htlcResource.setAccount1(htlcTaskData.getAccount1());
            htlcResource.setAccount2(htlcTaskData.getAccount2());
            htlcResource.setCounterpartyAddress(htlcTaskData.getCounterpartyAddress());
            htlcResource.setDriver(resource.getDriver());

            return htlcResource;
        }
        return resource;
    }

    public void initHTLCResourcePairs(ZoneManager zoneManager) {
        htlcResourcePairs = new HTLCResourcePair[htlcTaskDataMap.size()];
        int num = 0;
        for (HTLCTaskData htlcTaskData : htlcTaskDataMap.values()) {
            Path selfPath = htlcTaskData.getSelfPath();
            Path counterpartyPath = htlcTaskData.getCounterpartyPath();

            HTLCResource selfHTLCResource =
                    new HTLCResource(zoneManager, selfPath, counterpartyPath);
            selfHTLCResource.setAccount1(htlcTaskData.getAccount1());
            selfHTLCResource.setAccount2(htlcTaskData.getAccount2());
            selfHTLCResource.setCounterpartyAddress(htlcTaskData.getCounterpartyAddress());

            // no need to set counterpartyAddress
            HTLCResource counterpartyHTLCResource =
                    new HTLCResource(zoneManager, counterpartyPath, selfPath);
            counterpartyHTLCResource.setAccount1(htlcTaskData.getAccount2());
            counterpartyHTLCResource.setAccount2(htlcTaskData.getAccount1());

            HTLC weCrossHTLC = new AssetHTLC();
            htlcResourcePairs[num++] =
                    new HTLCResourcePair(weCrossHTLC, selfHTLCResource, counterpartyHTLCResource);
        }
    }

    public Map<String, HTLCTaskData> getHtlcTaskDataMap() {
        return htlcTaskDataMap;
    }

    public void setHtlcTaskDataMap(Map<String, HTLCTaskData> htlcTaskDataMap) {
        this.htlcTaskDataMap = htlcTaskDataMap;
    }

    public HTLCResourcePair[] getHtlcResourcePairs() {
        return htlcResourcePairs;
    }

    public void setHtlcResourcePairs(HTLCResourcePair[] htlcResourcePairs) {
        this.htlcResourcePairs = htlcResourcePairs;
    }
}
