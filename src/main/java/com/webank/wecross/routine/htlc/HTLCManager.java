package com.webank.wecross.routine.htlc;

import com.webank.wecross.resource.Resource;
import com.webank.wecross.stub.Path;
import com.webank.wecross.zone.ZoneManager;
import java.util.HashMap;
import java.util.Map;

public class HTLCManager {
    Map<String, HTLCTaskInfo> htlcTaskInfos = new HashMap<>();

    public Resource filterHTLCResource(ZoneManager zoneManager, Path path, Resource resource) {
        if (htlcTaskInfos.containsKey(path.toString())) {
            try {
                HTLCTaskInfo htlcTaskInfo = htlcTaskInfos.get(path.toString());
                String counterpartyPath = htlcTaskInfo.getCounterpartyPath();
                String counterpartyAddress = htlcTaskInfo.getCounterpartyAddress();
                Resource counterpartyResource =
                        zoneManager.getResource(Path.decode(counterpartyPath));
                return new HTLCResource(true, resource, counterpartyResource, counterpartyAddress);
            } catch (Exception e) {
                return null;
            }
        }
        return resource;
    }

    public Map<String, HTLCTaskInfo> getHtlcTaskInfos() {
        return htlcTaskInfos;
    }

    public void setHtlcTaskInfos(Map<String, HTLCTaskInfo> htlcTaskInfos) {
        this.htlcTaskInfos = htlcTaskInfos;
    }
}
