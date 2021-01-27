package com.webank.wecross.routine.htlc;

import com.webank.wecross.polling.Task;
import com.webank.wecross.polling.TaskManager;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.routine.RoutineDefault;
import com.webank.wecross.stub.Path;
import com.webank.wecross.zone.ZoneManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTLCManager {
    private Logger logger = LoggerFactory.getLogger(HTLCManager.class);

    private ZoneManager zoneManager;
    private Map<String, HTLCContext> htlcContextMap = new HashMap<>();

    public void registerTask(TaskManager taskManager) {
        if (Objects.isNull(zoneManager)) {
            logger.error("HTLCManager has not been initialized");
            return;
        }

        HTLCResourcePair[] htlcResourcePairs = initHTLCResourcePairs();
        if (Objects.nonNull(htlcResourcePairs) && htlcResourcePairs.length > 0) {
            HTLCTaskFactory htlcTaskFactory = new HTLCTaskFactory();
            Task[] tasks =
                    htlcTaskFactory.load(
                            htlcResourcePairs, RoutineDefault.HTLC_JOB_DATA_KEY, HTLCJob.class);
            taskManager.registerTasks(tasks);
        }
    }

    public Resource filterHTLCResource(ZoneManager zoneManager, Path path, Resource resource) {
        if (htlcContextMap.containsKey(path.toString())) {
            HTLCContext htlcContext = htlcContextMap.get(path.toString());

            HTLCResource htlcResource =
                    new HTLCResource(zoneManager, path, htlcContext.getCounterpartyPath());
            htlcResource.setAdminUa(htlcContext.getAdminUA());
            htlcResource.setDriver(resource.getDriver());

            return htlcResource;
        }
        return resource;
    }

    private HTLCResourcePair[] initHTLCResourcePairs() {
        HTLCResourcePair[] htlcResourcePairs = new HTLCResourcePair[htlcContextMap.size()];
        int num = 0;
        for (HTLCContext htlcContext : htlcContextMap.values()) {
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
        return htlcResourcePairs;
    }

    public ZoneManager getZoneManager() {
        return zoneManager;
    }

    public void setZoneManager(ZoneManager zoneManager) {
        this.zoneManager = zoneManager;
    }

    public Map<String, HTLCContext> getHtlcContextMap() {
        return htlcContextMap;
    }

    public void setHtlcContextMap(Map<String, HTLCContext> htlcContextMap) {
        this.htlcContextMap = htlcContextMap;
    }
}
